package kqlhotel.bus.checkout;

import kqlhotel.dao.invoice.InvoiceDAO;
import kqlhotel.dao.invoice.InvoiceDetailDAO;
import kqlhotel.dao.invoice.ServiceDetailDAO;
import kqlhotel.dao.promotion.PromotionDAO;
import kqlhotel.dao.room.RoomDAO;
import kqlhotel.dao.room.RoomTypeDAO;
import kqlhotel.entity.Invoice;
import kqlhotel.entity.InvoiceDetail;
import kqlhotel.entity.Promotion;
import kqlhotel.entity.Room;
import kqlhotel.entity.RoomType;
import kqlhotel.entity.ServiceDetail;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CheckoutBUS {
    private final InvoiceDAO invoiceDAO = new InvoiceDAO();
    private final InvoiceDetailDAO invoiceDetailDAO = new InvoiceDetailDAO();
    private final ServiceDetailDAO serviceDetailDAO = new ServiceDetailDAO();
    private final PromotionDAO promotionDAO = new PromotionDAO();
    private final RoomDAO roomDAO = new RoomDAO();
    private final RoomTypeDAO roomTypeDAO = new RoomTypeDAO();

    public Invoice getInvoiceForCheckout(String maPhong) {
        Invoice hd = invoiceDAO.getActiveByRoom(maPhong);

        if (hd == null) {
            hd = getActiveByRoomFromBooking(maPhong);
        }

        if (hd == null) {
            return null;
        }

        recalculateInvoiceTotals(hd, null);
        return hd;
    }

    public void applyPromotionToInvoice(Invoice hd, String maKM) {
        if (hd == null) {
            return;
        }
        recalculateInvoiceTotals(hd, maKM);
    }

    public List<Promotion> getAvailablePromotions() {
        List<Promotion> all = promotionDAO.getAll();
        List<Promotion> available = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (Promotion km : all) {
            if (km == null) {
                continue;
            }

            boolean statusOk = "DangHoatDong".equalsIgnoreCase(km.getTrangThaiKM())
                    || "SapDienRa".equalsIgnoreCase(km.getTrangThaiKM());

            boolean timeOk = true;
            if (km.getNgayBatDau() != null && now.isBefore(km.getNgayBatDau())) {
                timeOk = false;
            }
            if (km.getNgayKetThuc() != null && now.isAfter(km.getNgayKetThuc())) {
                timeOk = false;
            }

            if (statusOk && timeOk) {
                available.add(km);
            }
        }

        available.sort((a, b) -> Double.compare(b.getTienKhuyenMai(), a.getTienKhuyenMai()));
        return available;
    }

    public boolean completeCheckout(Invoice hd, List<String> roomCodes, String nextRoomStatus, String maKM) {
        if (hd == null || roomCodes == null || roomCodes.isEmpty()) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();

        // 1. Nếu chưa có ChiTietHoaDon thì tạo từ ChiTietDatPhong
        invoiceDetailDAO.createFromBookingIfMissing(
                hd.getMaHD(),
                hd.getMaDatPhong(),
                roomCodes
        );

        // 2. BẮT BUỘC load lại sau khi tạo
        List<InvoiceDetail> chiTietRooms = invoiceDetailDAO.getByInvoice(hd.getMaHD());

        if (chiTietRooms == null || chiTietRooms.isEmpty()) {
            return false;
        }

        boolean allUpdated = true;
        boolean hasTargetRoom = false;

        for (InvoiceDetail ct : chiTietRooms) {
            if (!roomCodes.contains(ct.getMaPhong())) {
                continue;
            }

            hasTargetRoom = true;

            if (ct.getNgayTraThucTe() != null) {
                continue;
            }

            Room p = roomDAO.getById(ct.getMaPhong());
            if (p == null) {
                allUpdated = false;
                continue;
            }

            RoomType lp = roomTypeDAO.getById(p.getLoaiPhong());
            if (lp == null) {
                allUpdated = false;
                continue;
            }

            long days = Duration.between(ct.getNgayNhanPhong(), now).toDays();
            if (days <= 0) {
                days = 1;
            }

            double fee = days * lp.getGiaPhong() + ct.getPhuThu();

            boolean updateDetail = invoiceDetailDAO.updateCheckoutInfo(
                    hd.getMaHD(),
                    ct.getMaPhong(),
                    now,
                    (int) days,
                    fee
            );

            boolean updateRoom = roomDAO.updateStatus(
                    ct.getMaPhong(),
                    normalizeRoomStatus(nextRoomStatus)
            );

            if (!updateDetail || !updateRoom) {
                allUpdated = false;
            }
        }

        if (!hasTargetRoom) {
            return false;
        }

        // 3. Load lại sau khi update checkout
        recalculateInvoiceTotals(hd, maKM);

        List<InvoiceDetail> updatedRoomDetails = invoiceDetailDAO.getByInvoice(hd.getMaHD());

        int paidRooms = 0;
        for (InvoiceDetail ct : updatedRoomDetails) {
            if (ct.getNgayTraThucTe() != null) {
                paidRooms++;
            }
        }

        if (paidRooms == updatedRoomDetails.size() && !updatedRoomDetails.isEmpty()) {
            hd.setTrangThai("DaThanhToan");
            hd.setNgayThanhToan(now);
        } else {
            hd.setTrangThai("ChuaThanhToan");
            hd.setNgayThanhToan(null);
        }

        boolean updateHD = invoiceDAO.update(hd);

        return allUpdated && updateHD;
    }

    private void recalculateInvoiceTotals(Invoice hd, String maKM) {
        List<InvoiceDetail> chiTietRooms = invoiceDetailDAO.getByInvoice(hd.getMaHD());

        if ((chiTietRooms == null || chiTietRooms.isEmpty())
                && hd.getMaDatPhong() != null
                && !hd.getMaDatPhong().isBlank()) {
            chiTietRooms = invoiceDetailDAO.getByBooking(hd.getMaHD(), hd.getMaDatPhong());
        }

        double totalRoomFee = 0;
        for (InvoiceDetail ct : chiTietRooms) {
            totalRoomFee += ct.getThanhTien();
        }

        hd.setTienPhong(totalRoomFee);

        List<ServiceDetail> chiTietServices = serviceDetailDAO.getByInvoice(hd.getMaHD());
        double totalServiceFee = 0;
        for (ServiceDetail ct : chiTietServices) {
            totalServiceFee += ct.getThanhTien();
        }
        hd.setTienDichVu(totalServiceFee);

        double subTotal = totalRoomFee + totalServiceFee;
        double tax = subTotal * 0.1;
        double discount = calculatePromotionDiscount(maKM, subTotal + tax);

        hd.setMaKhuyenMai((maKM == null || maKM.isBlank()) ? null : maKM);
        hd.setTienKhuyenMai(discount);
        hd.setTienThue(tax);

        double finalTotal = Math.max(0, subTotal + tax - discount);
        hd.setTongTienThanhToan(finalTotal);
    }

    private double calculatePromotionDiscount(String maKM, double amountBeforeDiscount) {
        if (maKM == null || maKM.isBlank()) {
            return 0;
        }

        Promotion km = promotionDAO.getById(maKM);
        if (km == null) {
            return 0;
        }

        double discount = km.getTienKhuyenMai();

        if (km.getGiaTriToiDa() > 0) {
            discount = Math.min(discount, km.getGiaTriToiDa());
        }

        return Math.max(0, Math.min(discount, amountBeforeDiscount));
    }

    private String normalizeRoomStatus(String nextRoomStatus) {
        if (nextRoomStatus == null) {
            return "Trong";
        }
        if ("Bảo trì".equalsIgnoreCase(nextRoomStatus) || "Bao tri".equalsIgnoreCase(nextRoomStatus)) {
            return "BaoTri";
        }
        return "Trong";
    }

    public List<kqlhotel.gui.tabs.CheckoutPanel.CheckoutData> searchCheckoutData(String roomCode, String cusId, String cusName) {
        List<kqlhotel.gui.tabs.CheckoutPanel.CheckoutData> list = new ArrayList<>();

        try {
            java.sql.Connection con = kqlhotel.dao.ConnectDB.getInstance().getConnection();

            StringBuilder sql = new StringBuilder(
                    "SELECT hd.maHD, p.maPhong, lp.tenLoaiPhong, kh.hoTenKH, kh.maKH, kh.sdt, " +
                            "COALESCE(cthd.ngayNhanPhong, ctdp.ngayNhanDuKien) AS ngayNhanPhong, " +
                            "COALESCE(cthd.ngayTraPhong, ctdp.ngayTraDuKien) AS ngayTraPhong, " +
                            "lp.giaPhong, cthd.ngayTraThucTe " +
                            "FROM HoaDon hd " +
                            "JOIN DatPhong dp ON hd.maDatPhong = dp.maDatPhong " +
                            "JOIN ChiTietDatPhong ctdp ON dp.maDatPhong = ctdp.maDatPhong " +
                            "LEFT JOIN ChiTietHoaDon cthd ON hd.maHD = cthd.maHD AND ctdp.maPhong = cthd.maPhong " +
                            "JOIN Phong p ON ctdp.maPhong = p.maPhong " +
                            "JOIN LoaiPhong lp ON p.maLoaiPhong = lp.maLoaiPhong " +
                            "JOIN KhachHang kh ON hd.maKH = kh.maKH " +
                            "WHERE hd.trangThai = 'ChuaThanhToan' " +
                            "AND (cthd.ngayTraThucTe IS NULL) "
            );

            if (roomCode != null && !roomCode.isEmpty()) {
                sql.append("AND (p.maPhong LIKE ? OR hd.maHD LIKE ?) ");
            }
            if (cusId != null && !cusId.isEmpty()) {
                sql.append("AND kh.maKH LIKE ? ");
            }
            if (cusName != null && !cusName.isEmpty()) {
                sql.append("AND kh.hoTenKH LIKE ? ");
            }

            sql.append("ORDER BY ngayTraPhong ASC, hd.maHD ASC");

            java.sql.PreparedStatement pstmt = con.prepareStatement(sql.toString());
            int idx = 1;

            if (roomCode != null && !roomCode.isEmpty()) {
                pstmt.setString(idx++, "%" + roomCode + "%");
                pstmt.setString(idx++, "%" + roomCode + "%");
            }
            if (cusId != null && !cusId.isEmpty()) {
                pstmt.setString(idx++, "%" + cusId + "%");
            }
            if (cusName != null && !cusName.isEmpty()) {
                pstmt.setString(idx++, "%" + cusName + "%");
            }

            java.sql.ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String id = rs.getString("maHD");
                String rName = "Phòng " + rs.getString("maPhong") + " · " + rs.getString("tenLoaiPhong");
                String cName = rs.getString("hoTenKH");
                String phone = rs.getString("sdt");
                String dateIn = kqlhotel.utils.DateUtils.format(rs.getTimestamp("ngayNhanPhong").toLocalDateTime());
                String dateOut = kqlhotel.utils.DateUtils.format(rs.getTimestamp("ngayTraPhong").toLocalDateTime());
                String price = kqlhotel.utils.CurrencyUtils.formatVND(rs.getDouble("giaPhong")) + "/đêm";

                String statusText = "Đang ở";
                java.awt.Color statusColor = new java.awt.Color(240, 60, 60);

                list.add(new kqlhotel.gui.tabs.CheckoutPanel.CheckoutData(
                        id, rName, cName, phone, dateIn, dateOut, price, statusText, statusColor
                ));
            }
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public List<kqlhotel.gui.tabs.CheckoutPanel.CheckoutData> getRoomsDueToday() {
        List<kqlhotel.gui.tabs.CheckoutPanel.CheckoutData> list = new ArrayList<>();
        try {
            java.sql.Connection con = kqlhotel.dao.ConnectDB.getInstance().getConnection();
            String sql =
                    "SELECT hd.maHD, p.maPhong, lp.tenLoaiPhong, kh.hoTenKH, kh.maKH, kh.sdt, " +
                            "cthd.ngayNhanPhong, cthd.ngayTraPhong, lp.giaPhong, cthd.ngayTraThucTe " +
                            "FROM HoaDon hd " +
                            "JOIN ChiTietHoaDon cthd ON hd.maHD = cthd.maHD " +
                            "JOIN Phong p ON cthd.maPhong = p.maPhong " +
                            "JOIN LoaiPhong lp ON p.maLoaiPhong = lp.maLoaiPhong " +
                            "JOIN KhachHang kh ON hd.maKH = kh.maKH " +
                            "WHERE hd.trangThai IN (N'ChuaThanhToan', N'DaThanhToan') " +
                            "AND cthd.ngayTraThucTe IS NULL " +
                            "AND CAST(cthd.ngayTraPhong AS DATE) = CAST(GETDATE() AS DATE) " +
                            "ORDER BY cthd.ngayTraPhong ASC, hd.maHD ASC";

            java.sql.PreparedStatement pstmt = con.prepareStatement(sql);
            java.sql.ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String id = rs.getString("maHD");
                String rName = "Phòng " + rs.getString("maPhong") + " · " + rs.getString("tenLoaiPhong");
                String cName = rs.getString("hoTenKH");
                String phone = rs.getString("sdt");
                String dateIn = kqlhotel.utils.DateUtils.format(rs.getTimestamp("ngayNhanPhong").toLocalDateTime());
                String dateOut = kqlhotel.utils.DateUtils.format(rs.getTimestamp("ngayTraPhong").toLocalDateTime());
                String price = kqlhotel.utils.CurrencyUtils.formatVND(rs.getDouble("giaPhong")) + "/đêm";

                String statusText = "Trả hôm nay";
                java.awt.Color statusColor = new java.awt.Color(240, 60, 60);

                list.add(new kqlhotel.gui.tabs.CheckoutPanel.CheckoutData(
                        id, rName, cName, phone, dateIn, dateOut, price, statusText, statusColor
                ));
            }
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    private Invoice getActiveByRoomFromBooking(String maPhong) {
        try {
            java.sql.Connection con = kqlhotel.dao.ConnectDB.getInstance().getConnection();

            String sql =
                    "SELECT TOP 1 hd.* " +
                            "FROM HoaDon hd " +
                            "JOIN ChiTietDatPhong ctdp ON hd.maDatPhong = ctdp.maDatPhong " +
                            "WHERE ctdp.maPhong = ? " +
                            "AND hd.trangThai = 'ChuaThanhToan' " +
                            "ORDER BY hd.ngayLapHD DESC";

            java.sql.PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, maPhong);

            java.sql.ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return invoiceDAO.getById(rs.getString("maHD"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}