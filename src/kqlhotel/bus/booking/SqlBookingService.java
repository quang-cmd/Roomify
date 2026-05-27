package kqlhotel.bus.booking;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import kqlhotel.bus.payment.PaymentBUS;
import kqlhotel.bus.shift.ShiftBUS;
import kqlhotel.dao.ConnectDB;
import kqlhotel.dao.booking.BookingDao;
import kqlhotel.dao.booking.BookingDaoSqlServer;
import kqlhotel.dao.booking.RoomDao;
import kqlhotel.dao.booking.RoomDaoSqlServer;
import kqlhotel.entity.BookingEntity;
import kqlhotel.entity.BookingRoomEntity;
import kqlhotel.entity.Customer;
import kqlhotel.entity.GuestStayDetail;
import kqlhotel.entity.Invoice;
import kqlhotel.entity.Payment;
import kqlhotel.entity.RoomEntity;
import kqlhotel.service.EmailService;

public class SqlBookingService implements BookingService {
    private static final int CHECK_IN_HOUR = 14;
    private static final int CHECK_OUT_HOUR = 12;
    private static final double VAT_RATE = 0.10;
    private final RoomDao roomDao;
    private final BookingDao bookingDao;
    private final PaymentBUS paymentBUS = new PaymentBUS();
    private final ShiftBUS shiftBUS = new ShiftBUS();

    public SqlBookingService() {
        this(new RoomDaoSqlServer(), new BookingDaoSqlServer());
    }

    public SqlBookingService(RoomDao roomDao) {
        this(roomDao, new BookingDaoSqlServer());
    }

    public SqlBookingService(RoomDao roomDao, BookingDao bookingDao) {
        this.roomDao = roomDao;
        this.bookingDao = bookingDao;
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
            long nightlyPrice = row.getNightlyPrice();
            if (!matchesPriceRange(nightlyPrice, request.getMinPrice(), request.getMaxPrice())) {
                continue;
            }
            String status = row.getAvailableRooms() + "/" + row.getTotalRooms();
            roomOptions.add(new RoomOptionDto(
                row.getRoomType(),
                nightlyPrice,
                row.getMaxGuests(),
                row.getMaxChildren(),
                status,
                row.getAvailableRooms(),
                row.getAmenities()
            ));
        }
        return roomOptions;
    }

    private boolean matchesPriceRange(long nightlyPrice, Long minPrice, Long maxPrice) {
        if (minPrice != null && nightlyPrice < minPrice) {
            return false;
        }
        return maxPrice == null || nightlyPrice < maxPrice;
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
        for (RoomOptionDto selectedRoom : selectedRooms) {
            roomTotalPerNight += selectedRoom.getNightlyPrice();
        }

        long totalAmount = calculateFinalTotal(roomTotalPerNight * nights);

        return new BookingSelectionSummary(selectedRooms.size(), nights, totalAmount);
    }

    @Override
    public BookingConfirmationResult createBooking(CreateBookingCommand command) {
        if (command == null) {
            return fail("Dữ liệu đặt phòng không hợp lệ.");
        }
        if (command.getSelectedRooms() == null || command.getSelectedRooms().isEmpty()) {
            return fail("Vui lòng chọn ít nhất 1 phòng.");
        }
        if (command.getGuestInfos() == null || command.getGuestInfos().isEmpty()) {
            return fail("Vui lòng nhập thông tin khách hàng.");
        }
        if (command.getCheckInDate() == null || command.getCheckOutDate() == null
            || !command.getCheckOutDate().isAfter(command.getCheckInDate())) {
            return fail("Ngày nhận/trả phòng không hợp lệ.");
        }

        int adults = command.getAdults();
        int children = command.getChildren();
        int sumAdultCapacity = 0;
        int sumTotalCapacity = 0;
        for (RoomOptionDto option : command.getSelectedRooms()) {
            int adultCapacity = Math.max(0, option.getMaxGuests());
            int childExtraCapacity = Math.max(0, option.getMaxChildren());
            sumAdultCapacity += adultCapacity;
            sumTotalCapacity += adultCapacity + childExtraCapacity;
        }

        if (adults > sumAdultCapacity) {
            return fail("Số lượng người lớn (" + adults + ") vượt quá sức chứa người lớn tối đa (" + sumAdultCapacity + "). Vui lòng chọn thêm phòng.");
        }

        int totalGuests = adults + children;
        if (totalGuests > sumTotalCapacity) {
            return fail("Tổng số khách (" + totalGuests + ") vượt quá sức chứa tổng (" + sumTotalCapacity + "). Vui lòng chọn thêm phòng.");
        }

        double ratio = command.getPaymentRatio();
        if (Double.isNaN(ratio) || Double.isInfinite(ratio) || ratio <= 0 || ratio > 1.0) {
            return fail("Tỉ lệ thanh toán không hợp lệ (phải trong khoảng 0 < tỉ lệ <= 1).");
        }

        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) {
            return fail("Không thể kết nối đến cơ sở dữ liệu.");
        }

        boolean originalAutoCommit = true;
        try {
            originalAutoCommit = con.getAutoCommit();
            con.setAutoCommit(false);

            // 1. Allocate physical rooms (maPhong) for each selected room option
            List<String> allocatedRoomIds = new ArrayList<>();
            Set<String> usedRoomIds = new HashSet<>();
            for (RoomOptionDto option : command.getSelectedRooms()) {
                String roomId = bookingDao.pickAvailableRoomId(con, option.getRoomType(),
                    command.getCheckInDate(), command.getCheckOutDate(), usedRoomIds);
                if (roomId == null) {
                    con.rollback();
                    return fail("Không còn phòng trống cho loại: " + option.getRoomType());
                }
                allocatedRoomIds.add(roomId);
                usedRoomIds.add(roomId);
            }

            // 2. Upsert KhachHang for adult guests; use lead adult guest as booking owner.
            // Children can stay in ChiTietKhachO without CCCD/phone.
            String leadCustomerId = null;
            int adultGuestCount = Math.max(1, command.getAdults());
            for (int i = 0; i < command.getGuestInfos().size(); i++) {
                if (i >= adultGuestCount) {
                    continue;
                }

                GuestInfoDto guest = command.getGuestInfos().get(i);
                String phoneConflict = bookingDao.findCustomerNameByPhoneWithDifferentId(con, guest.getPhone(), guest.getIdNo());
                if (phoneConflict != null) {
                    con.rollback();
                    return fail("Số điện thoại " + guest.getPhone().trim()
                        + " đã thuộc khách hàng khác (" + phoneConflict
                        + "). Vui lòng kiểm tra lại CCCD hoặc dùng số điện thoại khác.");
                }

                String maKH = bookingDao.upsertCustomer(con, toCustomer(guest));
                if (maKH == null) {
                    con.rollback();
                    return fail("Không thể lưu thông tin khách hàng: " + guest.getFullName());
                }
                if (i == 0) {
                    leadCustomerId = maKH;
                }
            }
            if (leadCustomerId == null) {
                con.rollback();
                return fail("Không thể xác định khách đại diện cho đặt phòng.");
            }

            // 3. Resolve maNV from the logged-in staff, fallback only for legacy callers.
            String maNV = command.getStaffId();
            if (maNV == null || maNV.isBlank()) {
                maNV = bookingDao.resolveStaffId(con);
            }
            if (maNV == null) {
                con.rollback();
                return fail("Không tìm thấy nhân viên xử lý.");
            }

            // 4. Insert DatPhong
            LocalDateTime now = LocalDateTime.now();
            // Policy khách sạn: nhận phòng 14:00, trả phòng 12:00 ngày trả.
            LocalDateTime checkInTs = command.getCheckInDate().atTime(CHECK_IN_HOUR, 0);
            LocalDateTime checkOutTs = command.getCheckOutDate().atTime(CHECK_OUT_HOUR, 0);
            LocalDateTime ngayDat = now;

            // Pre-compute totals so we can persist tienCoc on DatPhong
            int nights = Math.max(1, (int) ChronoUnit.DAYS.between(command.getCheckInDate(), command.getCheckOutDate()));
            long tongTien = command.getTotalAmount();
            long tienPhong = 0;
            if (tongTien <= 0) {
                long perNight = 0;
                for (RoomOptionDto r : command.getSelectedRooms()) {
                    perNight += r.getNightlyPrice();
                }
                tienPhong = perNight * nights;
                tongTien = calculateFinalTotal(tienPhong);
            } else {
                tienPhong = calculateRoomSubtotalFromFinalTotal(tongTien);
            }
            long tienThue = Math.max(0L, tongTien - tienPhong);
            long paidAmount = Math.round(tongTien * command.getPaymentRatio());
            // Store the amount collected at booking time, including full upfront payment.
            long tienCocBooking = paidAmount;
            String trangThaiHD = command.isFullyPaid() ? "DaThanhToan" : "ChuaThanhToan";

            BookingEntity booking = new BookingEntity();
            booking.setNgayDat(ngayDat);
            booking.setTienCoc(tienCocBooking);
            booking.setTrangThaiDatPhong("DaDat");
            booking.setGhiChu(command.isFullyPaid() ? "Thanh toan 100%" : "Dat coc 30%");
            booking.setMaKH(leadCustomerId);
            booking.setMaNV(maNV);
            String maDatPhong = bookingDao.createBooking(con, booking);

            // 5. Insert ChiTietDatPhong for each allocated room (composite PK, no maCTDP column)
            int numRooms = allocatedRoomIds.size();
            int totalPeople = command.getAdults() + command.getChildren();
            int guestsPerRoom = Math.max(1, (int) Math.ceil((double) totalPeople / numRooms));

            List<BookingRoomEntity> bookingRooms = new ArrayList<>();
            for (int i = 0; i < allocatedRoomIds.size(); i++) {
                BookingRoomEntity room = new BookingRoomEntity();
                room.setMaDatPhong(maDatPhong);
                room.setMaPhong(allocatedRoomIds.get(i));
                room.setNgayNhanDuKien(checkInTs);
                room.setNgayTraDuKien(checkOutTs);
                room.setDonGiaDat(command.getSelectedRooms().get(i).getNightlyPrice());
                room.setSoLuongNguoiO(Math.min(guestsPerRoom, Math.max(1, totalPeople)));
                room.setGhiChu("");
                bookingRooms.add(room);
            }
            bookingDao.createBookingRooms(con, bookingRooms);

            // 5.5. Insert guests to ChiTietKhachO
            List<GuestStayDetail> guestStayDetails = new ArrayList<>();
            for (int i = 0; i < command.getGuestInfos().size(); i++) {
                GuestInfoDto g = command.getGuestInfos().get(i);
                boolean childGuest = i >= adultGuestCount;
                // Distribute guests among booked rooms
                String roomId = allocatedRoomIds.get(i % allocatedRoomIds.size());
                
                String fallbackCccd = "CCCD_" + System.currentTimeMillis() + "_" + i;
                guestStayDetails.add(new GuestStayDetail(
                    maDatPhong,
                    roomId,
                    safeGuestName(g.getFullName(), i),
                    g.getIdNo() != null && !g.getIdNo().isEmpty() ? g.getIdNo() : fallbackCccd,
                    childGuest ? null : g.getPhone(),
                    i == 0 ? "Nguoi dai dien" : "Khach luu tru"
                ));
            }
            bookingDao.createGuestStayDetails(con, guestStayDetails);


            // 6. Insert HoaDon
            Invoice invoice = new Invoice();
            invoice.setNgayLapHD(now);
            invoice.setNgayThanhToan(command.isFullyPaid() ? now : null);
            invoice.setGhiChu(command.isFullyPaid() ? "Thanh toan 100% khi dat phong" : "Dat coc 30% khi dat phong");
            invoice.setSoLuongNguoi(Math.max(1, totalPeople));
            invoice.setTienPhong(tienPhong);
            invoice.setTienThue(tienThue);
            invoice.setTongTienThanhToan(tongTien);
            invoice.setMaKhachHang(leadCustomerId);
            invoice.setMaNhanVien(maNV);
            invoice.setPhuongThucTT(command.getPaymentMethod());
            invoice.setTrangThai(trangThaiHD);
            invoice.setMaDatPhong(maDatPhong);
            String maHD = bookingDao.createInvoice(con, invoice);

            // 7. Record payment collected at booking time.
            String note = command.isFullyPaid() ? "Thanh toan 100%" : "Dat coc 30%";
            if (command.getPaymentReference() != null && !command.getPaymentReference().isEmpty()) {
                note = note + " - Ref: " + command.getPaymentReference();
            }

            String maPC = shiftBUS.getOpenShiftIdByStaff(maNV);
            if (maPC == null || maPC.isBlank()) {
                con.rollback();
                return fail("Nhân viên chưa mở ca, không thể ghi nhận thanh toán đặt phòng.");
            }

            Payment payment = new Payment(
                paymentBUS.getNextId(con),
                now,
                paidAmount,
                note,
                command.getPaymentMethod(),
                "ThanhToanThanhCong",
                "Thu",
                maHD,
                maPC,
                maNV
            );

            if (!paymentBUS.recordPayment(con, payment)) {
                con.rollback();
                return fail("Không thể lưu thông tin thanh toán.");
            }

            con.commit();
            String emailNote = sendBookingEmailIfPossible(command, maDatPhong, maHD, tongTien, paidAmount);
            return new BookingConfirmationResult(true, maDatPhong,
                "Đặt phòng thành công. Mã đặt phòng: " + maDatPhong + ", mã hóa đơn: " + maHD + emailNote);
        } catch (SQLException ex) {
            ex.printStackTrace();
            try { con.rollback(); } catch (SQLException ignored) {}
            return fail("Lỗi cơ sở dữ liệu: " + ex.getMessage());
        } finally {
            try { con.setAutoCommit(originalAutoCommit); } catch (SQLException ignored) {}
        }
    }

    private BookingConfirmationResult fail(String message) {
        return new BookingConfirmationResult(false, null, message);
    }

    private long calculateFinalTotal(long roomSubtotal) {
        return Math.round(roomSubtotal * (1.0 + VAT_RATE));
    }

    private long calculateRoomSubtotalFromFinalTotal(long finalTotal) {
        return Math.round(finalTotal / (1.0 + VAT_RATE));
    }

    private String sendBookingEmailIfPossible(CreateBookingCommand command, String maDatPhong, String maHD,
                                              long totalAmount, long paidAmount) {
        if (command.getGuestInfos() == null || command.getGuestInfos().isEmpty()) {
            return "";
        }

        GuestInfoDto leadGuest = command.getGuestInfos().get(0);
        String email = leadGuest.getEmail();
        if (email == null || email.trim().isEmpty()) {
            return "\nChưa gửi email xác nhận: khách đại diện chưa có email.";
        }

        try {
            EmailService.sendBookingConfirmation(
                email.trim(),
                leadGuest.getFullName(),
                leadGuest.getPhone(),
                leadGuest.getIdNo(),
                maDatPhong,
                maHD,
                command.getCheckInDate(),
                command.getCheckOutDate(),
                summarizeRoomTypes(command.getSelectedRooms()),
                totalAmount,
                paidAmount,
                Math.max(0L, totalAmount - paidAmount)
            );
            return "\nĐã gửi email xác nhận đến: " + email.trim();
        } catch (Exception ex) {
            ex.printStackTrace();
            return "\nĐặt phòng thành công nhưng chưa gửi được email xác nhận: " + ex.getMessage();
        }
    }

    private String summarizeRoomTypes(List<RoomOptionDto> rooms) {
        if (rooms == null || rooms.isEmpty()) {
            return "";
        }
        java.util.Map<String, Integer> counts = new java.util.LinkedHashMap<>();
        for (RoomOptionDto room : rooms) {
            counts.merge(room.getRoomType(), 1, Integer::sum);
        }
        StringBuilder builder = new StringBuilder();
        for (java.util.Map.Entry<String, Integer> entry : counts.entrySet()) {
            if (builder.length() > 0) {
                builder.append(", ");
            }
            builder.append(entry.getKey());
            if (entry.getValue() > 1) {
                builder.append(" x").append(entry.getValue());
            }
        }
        return builder.toString();
    }

    private Customer toCustomer(GuestInfoDto guest) {
        Customer customer = new Customer();
        customer.setHoTenKH(guest.getFullName());
        customer.setGioiTinh(true);
        customer.setNgaySinh(LocalDate.of(2000, 1, 1).atStartOfDay());
        customer.setEmail(trimToNull(guest.getEmail()));
        customer.setSdt(trimOrEmpty(guest.getPhone()));
        customer.setCCCD(trimOrEmpty(guest.getIdNo()));
        customer.setQuocTich("Viet Nam");
        customer.setHangKH("Dong");
        customer.setDiemTichLuy(0);
        return customer;
    }

    private String safeGuestName(String value, int index) {
        return value != null && !value.isBlank() ? value.trim() : "Khach phu " + index;
    }

    private String trimToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }

    private String trimOrEmpty(String value) {
        return value == null ? "" : value.trim();
    }
}
