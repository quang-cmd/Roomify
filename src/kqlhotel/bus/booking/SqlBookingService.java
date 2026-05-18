package kqlhotel.bus.booking;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import kqlhotel.dao.ConnectDB;
import kqlhotel.dao.booking.RoomDao;
import kqlhotel.dao.booking.RoomDaoSqlServer;
import kqlhotel.entity.RoomEntity;

public class SqlBookingService implements BookingService {
    private static final int CHECK_IN_HOUR = 14;
    private static final int CHECK_OUT_HOUR = 12;
    private final RoomDao roomDao;

    public SqlBookingService() {
        this(new RoomDaoSqlServer());
    }

    public SqlBookingService(RoomDao roomDao) {
        this.roomDao = roomDao;
    }

    @Override
    public List<String> getRoomTypes() {
        return roomDao.findAllRoomTypes();
    }

    @Override
    public List<RoomOptionDto> searchAvailableRooms(BookingSearchRequest request) {
        if (request == null || request.getCheckInDate() == null || request.getCheckOutDate() == null) {
            return Collections.emptyList();
        }

        List<RoomEntity> rows = roomDao.findAvailableRooms(
            request.getRoomType(),
            request.getCheckInDate(),
            request.getCheckOutDate(),
            request.getAdults()
        );
        if (rows == null || rows.isEmpty()) {
            return Collections.emptyList();
        }

        List<RoomOptionDto> roomOptions = new java.util.ArrayList<>();
        for (RoomEntity row : rows) {
            String status = row.getAvailableRooms() + "/" + row.getTotalRooms();
            roomOptions.add(new RoomOptionDto(
                row.getRoomType(),
                row.getNightlyPrice(),
                row.getMaxGuests(),
                row.getMaxChildren(),
                status,
                row.getAvailableRooms(),
                row.getAmenities()
            ));
        }
        return roomOptions;
    }

    @Override
    public BookingSelectionSummary summarizeSelection(List<RoomOptionDto> selectedRooms, BookingSearchRequest request) {
        if (selectedRooms == null || selectedRooms.isEmpty() || request == null) {
            return new BookingSelectionSummary(0, 0, 0);
        }

        LocalDate checkInDate = request.getCheckInDate();
        LocalDate checkOutDate = request.getCheckOutDate();
        int nights = (int) Math.max(1, ChronoUnit.DAYS.between(checkInDate, checkOutDate));

        long roomTotalPerNight = 0;
        int sumAdultCapacity = 0;
        for (RoomOptionDto selectedRoom : selectedRooms) {
            roomTotalPerNight += selectedRoom.getNightlyPrice();
            sumAdultCapacity += selectedRoom.getMaxGuests();
        }

        long totalAmount = roomTotalPerNight * nights;

        int extraBeds = Math.max(0, request.getAdults() - sumAdultCapacity);
        if (extraBeds > 0) {
            long avgRoomPricePerNight = selectedRooms.isEmpty() ? 0 : roomTotalPerNight / selectedRooms.size();
            long surchargePerNight = (long) (extraBeds * 0.20 * avgRoomPricePerNight);
            totalAmount += surchargePerNight * nights;
        }

        return new BookingSelectionSummary(selectedRooms.size(), nights, totalAmount);
    }

    @Override
    public BookingConfirmationResult createBooking(CreateBookingCommand command) {
        if (command == null) {
            return fail("Du lieu dat phong khong hop le.");
        }
        if (command.getSelectedRooms() == null || command.getSelectedRooms().isEmpty()) {
            return fail("Vui long chon it nhat 1 phong.");
        }
        if (command.getGuestInfos() == null || command.getGuestInfos().isEmpty()) {
            return fail("Vui long nhap thong tin khach hang.");
        }
        if (command.getCheckInDate() == null || command.getCheckOutDate() == null
            || !command.getCheckOutDate().isAfter(command.getCheckInDate())) {
            return fail("Ngay nhan/tra phong khong hop le.");
        }

        int adults = command.getAdults();
        int children = command.getChildren();
        int sumAdultCapacity = 0;
        int sumChildrenCapacity = 0;
        int numberOfRooms = command.getSelectedRooms().size();
        for (RoomOptionDto option : command.getSelectedRooms()) {
             sumAdultCapacity += option.getMaxGuests();
             sumChildrenCapacity += option.getMaxChildren();
        }
        
        if (children > sumChildrenCapacity) {
             return fail("Số lượng trẻ em (" + children + ") vượt quá quy định tối đa (" + sumChildrenCapacity + " bé/" + numberOfRooms + " phòng). Vui lòng chọn thêm phòng.");
        }
        
        int extraBeds = Math.max(0, adults - sumAdultCapacity);
        if (extraBeds > 2 * numberOfRooms) {
             return fail("Sức chứa không đủ. Mỗi phòng chỉ được kê thêm tối đa 2 giường phụ (Đang cần kê " + extraBeds + " giường cho " + numberOfRooms + " phòng).");
        }

        double ratio = command.getPaymentRatio();
        if (Double.isNaN(ratio) || Double.isInfinite(ratio) || ratio <= 0 || ratio > 1.0) {
            return fail("Ti le thanh toan khong hop le (phai trong khoang 0 < ratio <= 1).");
        }

        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) {
            return fail("Khong the ket noi den CSDL.");
        }

        boolean originalAutoCommit = true;
        try {
            originalAutoCommit = con.getAutoCommit();
            con.setAutoCommit(false);

            // 1. Allocate physical rooms (maPhong) for each selected room option
            List<String> allocatedRoomIds = new ArrayList<>();
            Set<String> usedRoomIds = new HashSet<>();
            for (RoomOptionDto option : command.getSelectedRooms()) {
                String roomId = pickAvailableRoomId(con, option.getRoomType(),
                    command.getCheckInDate(), command.getCheckOutDate(), usedRoomIds);
                if (roomId == null) {
                    con.rollback();
                    return fail("Khong con phong trong cho loai: " + option.getRoomType());
                }
                allocatedRoomIds.add(roomId);
                usedRoomIds.add(roomId);
            }

            // 2. Upsert KhachHang for the lead guest (and others); use lead guest as booking owner
            String leadCustomerId = null;
            for (int i = 0; i < command.getGuestInfos().size(); i++) {
                GuestInfoDto guest = command.getGuestInfos().get(i);
                String maKH = upsertCustomer(con, guest);
                if (maKH == null) {
                    con.rollback();
                    return fail("Khong the luu thong tin khach hang: " + guest.getFullName());
                }
                if (i == 0) {
                    leadCustomerId = maKH;
                }
            }

            // 3. Resolve maNV (default to first active staff)
            String maNV = resolveStaffId(con);
            if (maNV == null) {
                con.rollback();
                return fail("Khong tim thay nhan vien xu ly.");
            }

            // 4. Insert DatPhong
            LocalDateTime now = LocalDateTime.now();
            // Policy khách sạn: nhận phòng 14:00, trả phòng 12:00 ngày tra.
            LocalDateTime checkInTs = command.getCheckInDate().atTime(CHECK_IN_HOUR, 0);
            LocalDateTime checkOutTs = command.getCheckOutDate().atTime(CHECK_OUT_HOUR, 0);
            LocalDateTime ngayDat = now;

            // Pre-compute totals so we can persist tienCoc on DatPhong
            int nights = Math.max(1, (int) ChronoUnit.DAYS.between(command.getCheckInDate(), command.getCheckOutDate()));
            long tienPhong = command.getTotalAmount();
            if (tienPhong <= 0) {
                long perNight = 0;
                int cap = 0;
                for (RoomOptionDto r : command.getSelectedRooms()) {
                    perNight += r.getNightlyPrice();
                    cap += r.getMaxGuests();
                }
                int eb = Math.max(0, command.getAdults() - cap);
                if (eb > 0 && !command.getSelectedRooms().isEmpty()) {
                    perNight += eb * 0.20 * (perNight / command.getSelectedRooms().size());
                }
                tienPhong = perNight * nights;
            }
            long tongTien = tienPhong; // no service / promo / tax for now
            long paidAmount = Math.round(tienPhong * command.getPaymentRatio());
            // Deposit recorded on the booking itself (0 when fully paid up-front, otherwise = paidAmount)
            long tienCocBooking = command.isFullyPaid() ? 0L : paidAmount;
            String trangThaiHD = command.isFullyPaid() ? "DaThanhToan" : "ChuaThanhToan";

            String maDatPhong = nextId(con, "DatPhong", "maDatPhong", "DP", 5);
            String insertDatPhong = "INSERT INTO DatPhong (maDatPhong, ngayDat, tienCoc, ghiChu, maKH, maNV) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = con.prepareStatement(insertDatPhong)) {
                ps.setString(1, maDatPhong);
                ps.setTimestamp(2, Timestamp.valueOf(ngayDat));
                ps.setBigDecimal(3, java.math.BigDecimal.valueOf(tienCocBooking));
                ps.setString(4, command.isFullyPaid() ? "Thanh toan 100%" : "Dat coc 30%");
                ps.setString(5, leadCustomerId);
                ps.setString(6, maNV);
                ps.executeUpdate();
            }

            // 5. Insert ChiTietDatPhong for each allocated room (composite PK, no maCTDP column)
            int numRooms = allocatedRoomIds.size();
            int totalPeople = command.getAdults() + command.getChildren();
            int guestsPerRoom = Math.max(1, (int) Math.ceil((double) totalPeople / numRooms));

            String insertCtdp = "INSERT INTO ChiTietDatPhong (maDatPhong, maPhong, ngayNhanDuKien, ngayTraDuKien, donGiaDat, soLuongNguoiO, ghiChu) VALUES (?, ?, ?, ?, ?, ?, ?)";
            for (int i = 0; i < allocatedRoomIds.size(); i++) {
                String roomId = allocatedRoomIds.get(i);
                long unitPrice = command.getSelectedRooms().get(i).getNightlyPrice();
                try (PreparedStatement ps = con.prepareStatement(insertCtdp)) {
                    ps.setString(1, maDatPhong);
                    ps.setString(2, roomId);
                    ps.setTimestamp(3, Timestamp.valueOf(checkInTs));
                    ps.setTimestamp(4, Timestamp.valueOf(checkOutTs));
                    ps.setBigDecimal(5, java.math.BigDecimal.valueOf(unitPrice));
                    ps.setInt(6, Math.min(guestsPerRoom, Math.max(1, totalPeople)));
                    ps.setString(7, "");
                    ps.executeUpdate();
                }
            }

            // 6. Insert HoaDon
            String maHD = nextId(con, "HoaDon", "maHD", "HD", 5);
            String insertHoaDon = "INSERT INTO HoaDon (maHD, ngayLapHD, ngayThanhToan, ghiChu, soLuongNguoiO, tienPhong, tienDichVu, tienKhuyenMai, tienThue, tongTienThanhToan, phiDoiPhong, maKM, maKH, maNV, phuongThucTT, trangThai, maDatPhong) " +
                "VALUES (?, ?, ?, ?, ?, ?, 0, 0, 0, ?, 0, NULL, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = con.prepareStatement(insertHoaDon)) {
                ps.setString(1, maHD);
                ps.setTimestamp(2, Timestamp.valueOf(now));
                if (command.isFullyPaid()) {
                    ps.setTimestamp(3, Timestamp.valueOf(now));
                } else {
                    ps.setNull(3, java.sql.Types.TIMESTAMP);
                }
                ps.setString(4, command.isFullyPaid() ? "Thanh toan 100% khi dat phong" : "Dat coc 30% khi dat phong");
                ps.setInt(5, Math.max(1, totalPeople));
                ps.setBigDecimal(6, java.math.BigDecimal.valueOf(tienPhong));
                ps.setBigDecimal(7, java.math.BigDecimal.valueOf(tongTien));
                ps.setString(8, leadCustomerId);
                ps.setString(9, maNV);
                ps.setString(10, command.getPaymentMethod());
                ps.setString(11, trangThaiHD);
                ps.setString(12, maDatPhong);
                ps.executeUpdate();
            }

            // 7. Insert ThanhToan record (for the actual collected amount)
            String maTT = nextId(con, "ThanhToan", "maTT", "TT", 5);
            String insertTT = "INSERT INTO ThanhToan (maTT, ngayTT, soTienTT, ghiChu, phuongThucTT, trangThaiTT, maHD) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = con.prepareStatement(insertTT)) {
                ps.setString(1, maTT);
                ps.setTimestamp(2, Timestamp.valueOf(now));
                ps.setBigDecimal(3, java.math.BigDecimal.valueOf(paidAmount));
                String note = command.isFullyPaid() ? "Thanh toan 100%" : "Dat coc 30%";
                if (command.getPaymentReference() != null && !command.getPaymentReference().isEmpty()) {
                    note = note + " - Ref: " + command.getPaymentReference();
                }
                ps.setString(4, note);
                ps.setString(5, command.getPaymentMethod());
                ps.setString(6, "ThanhToanThanhCong");
                ps.setString(7, maHD);
                ps.executeUpdate();
            }

            con.commit();
            return new BookingConfirmationResult(true, maDatPhong,
                "Dat phong thanh cong. Ma DP: " + maDatPhong + ", Ma HD: " + maHD);
        } catch (SQLException ex) {
            ex.printStackTrace();
            try { con.rollback(); } catch (SQLException ignored) {}
            return fail("Loi CSDL: " + ex.getMessage());
        } finally {
            try { con.setAutoCommit(originalAutoCommit); } catch (SQLException ignored) {}
        }
    }

    private BookingConfirmationResult fail(String message) {
        return new BookingConfirmationResult(false, null, message);
    }

    private String pickAvailableRoomId(Connection con, String roomTypeName, LocalDate checkIn, LocalDate checkOut, Set<String> excludeRoomIds) throws SQLException {
        String sql = "SELECT p.maPhong FROM Phong p " +
            "JOIN LoaiPhong lp ON p.maLoaiPhong = lp.maLoaiPhong " +
            "WHERE lp.tenLoaiPhong = ? " +
            "AND p.trangThaiPhong <> 'BaoTri' " +
            "AND NOT EXISTS (" +
            "  SELECT 1 FROM ChiTietDatPhong ctdp " +
            "  JOIN HoaDon hd ON hd.maDatPhong = ctdp.maDatPhong " +
            "  WHERE ctdp.maPhong = p.maPhong " +
            "  AND hd.trangThai <> 'DaHuy' " +
            "  AND ? < ctdp.ngayTraDuKien AND ? > ctdp.ngayNhanDuKien" +
            ") " +
            "ORDER BY NEWID()";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, roomTypeName);
            ps.setTimestamp(2, Timestamp.valueOf(checkIn.atTime(CHECK_IN_HOUR, 0)));
            ps.setTimestamp(3, Timestamp.valueOf(checkOut.atTime(CHECK_OUT_HOUR, 0)));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String roomId = rs.getString(1);
                    if (!excludeRoomIds.contains(roomId)) {
                        return roomId;
                    }
                }
            }
        }
        return null;
    }

    private String upsertCustomer(Connection con, GuestInfoDto guest) throws SQLException {
        if (guest == null || guest.getIdNo() == null || guest.getIdNo().trim().isEmpty()) {
            return null;
        }
        String cccd = guest.getIdNo().trim();
        try (PreparedStatement ps = con.prepareStatement("SELECT maKH FROM KhachHang WHERE CCCD = ?")) {
            ps.setString(1, cccd);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString(1);
                }
            }
        }

        // Avoid SDT collision: if phone already exists for someone else, return that maKH
        if (guest.getPhone() != null && !guest.getPhone().trim().isEmpty()) {
            try (PreparedStatement ps = con.prepareStatement("SELECT maKH FROM KhachHang WHERE sdt = ?")) {
                ps.setString(1, guest.getPhone().trim());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getString(1);
                    }
                }
            }
        }

        String maKH = nextId(con, "KhachHang", "maKH", "KH", 5);
        String sql = "INSERT INTO KhachHang (maKH, hoTenKH, gioiTinh, ngaySinh, email, sdt, CCCD, quocTich, diaChi, hangKH, diemTichLuy) " +
            "VALUES (?, ?, 1, ?, NULL, ?, ?, N'Viet Nam', NULL, 'Dong', 0)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maKH);
            ps.setString(2, guest.getFullName());
            ps.setTimestamp(3, Timestamp.valueOf(LocalDate.of(2000, 1, 1).atStartOfDay()));
            ps.setString(4, guest.getPhone() == null ? "" : guest.getPhone().trim());
            ps.setString(5, cccd);
            ps.executeUpdate();
        }
        return maKH;
    }

    private String resolveStaffId(Connection con) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT TOP 1 nv.maNV FROM NhanVien nv JOIN TaiKhoan tk ON nv.tenDangNhap = tk.tenDangNhap " +
                "WHERE tk.trangThaiTK = 'DangHoatDong' ORDER BY nv.maNV")) {
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString(1);
                }
            }
        }
        return null;
    }

    private String nextId(Connection con, String table, String column, String prefix, int totalLength) throws SQLException {
        int nextSeq = nextSequence(con, table, column, prefix);
        int digits = totalLength - prefix.length();
        return prefix + String.format("%0" + digits + "d", nextSeq);
    }

    private int nextSequence(Connection con, String table, String column, String prefix) throws SQLException {
        String sql = "SELECT TOP 1 " + column + " FROM " + table + " WHERE " + column + " LIKE ? ORDER BY " + column + " DESC";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, prefix + "%");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String last = rs.getString(1);
                    String numPart = last.substring(prefix.length()).replaceAll("[^0-9]", "");
                    if (!numPart.isEmpty()) {
                        return Integer.parseInt(numPart) + 1;
                    }
                }
            }
        }
        return 1;
    }
}
