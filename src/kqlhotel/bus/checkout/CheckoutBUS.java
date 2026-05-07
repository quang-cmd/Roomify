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
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class CheckoutBUS {
    private final InvoiceDAO invoiceDAO = new InvoiceDAO();
    private final InvoiceDetailDAO invoiceDetailDAO = new InvoiceDetailDAO();
    private final ServiceDetailDAO serviceDetailDAO = new ServiceDetailDAO();
    private final PromotionDAO promotionDAO = new PromotionDAO();
    private final RoomDAO roomDAO = new RoomDAO();
    private final RoomTypeDAO roomTypeDAO = new RoomTypeDAO();

    public static class CheckoutTotals {
        public double roomFee;
        public double serviceFee;
        public double surcharge;
        public double tax;

        // Khuyến mãi theo mã khuyến mãi đang chọn
        public double discount;

        // Khuyến mãi theo hạng khách hàng
        public double rankDiscount;
        public double rankDiscountRate;

        // Tổng toàn bộ khuyến mãi = mã KM + hạng KH
        public double totalDiscount;

        public double total;
        public double earlyCheckoutPenalty;

        public CheckoutTotals(double roomFee, double serviceFee, double surcharge,
                              double tax, double discount, double total) {
            this(roomFee, serviceFee, surcharge, tax, discount, total, 0, 0, 0);
        }

        public CheckoutTotals(double roomFee, double serviceFee, double surcharge,
                              double tax, double discount, double total,
                              double earlyCheckoutPenalty) {
            this(roomFee, serviceFee, surcharge, tax, discount, total, earlyCheckoutPenalty, 0, 0);
        }

        public CheckoutTotals(double roomFee, double serviceFee, double surcharge,
                              double tax, double discount, double total,
                              double earlyCheckoutPenalty,
                              double rankDiscount,
                              double rankDiscountRate) {
            this.roomFee = roomFee;
            this.serviceFee = serviceFee;
            this.surcharge = surcharge;
            this.tax = tax;
            this.discount = discount;
            this.rankDiscount = rankDiscount;
            this.rankDiscountRate = rankDiscountRate;
            this.totalDiscount = discount + rankDiscount;
            this.total = total;
            this.earlyCheckoutPenalty = earlyCheckoutPenalty;
        }
    }

    private static class RoomCharge {
        int nights;
        double roomFee;
        double surcharge;
        double taxableRoomFee;
        double lateCheckoutPenalty;
        double total;

        RoomCharge(int nights, double roomFee, double surcharge,
                   double taxableRoomFee, double lateCheckoutPenalty) {
            this.nights = nights;
            this.roomFee = roomFee;
            this.surcharge = surcharge;
            this.taxableRoomFee = taxableRoomFee;
            this.lateCheckoutPenalty = lateCheckoutPenalty;
            this.total = roomFee + surcharge + lateCheckoutPenalty;
        }
    }

    public Invoice getInvoiceForCheckout(String maPhong) {
        Invoice hd = invoiceDAO.getActiveByRoom(maPhong);

        if (hd == null) {
            hd = getActiveByRoomFromBooking(maPhong);
        }

        return hd;
    }

    public void applyPromotionToInvoice(Invoice hd, String maKM) {
        if (hd == null) {
            return;
        }

        CheckoutTotals totals = previewTotals(hd, null, maKM);

        hd.setMaKhuyenMai((maKM == null || maKM.isBlank()) ? null : maKM);
        hd.setTienPhong(totals.roomFee);
        hd.setTienDichVu(totals.serviceFee);
        hd.setTienThue(totals.tax);
        hd.setTienKhuyenMai(totals.totalDiscount);
        hd.setTongTienThanhToan(totals.total);
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

        invoiceDetailDAO.createFromBookingIfMissing(
                hd.getMaHD(),
                hd.getMaDatPhong(),
                roomCodes
        );

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

            RoomCharge charge = calculateRoomCharge(hd, ct, now);

            boolean updateDetail = invoiceDetailDAO.updateCheckoutInfo(
                    hd.getMaHD(),
                    ct.getMaPhong(),
                    now,
                    charge.nights,
                    charge.surcharge,
                    charge.lateCheckoutPenalty,
                    charge.total
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

        CheckoutTotals totals = previewTotals(hd, null, maKM);

        hd.setMaKhuyenMai((maKM == null || maKM.isBlank()) ? null : maKM);
        hd.setTienPhong(totals.roomFee);
        hd.setTienDichVu(totals.serviceFee);
        hd.setTienThue(totals.tax);
        hd.setTienKhuyenMai(totals.totalDiscount);
        hd.setTongTienThanhToan(totals.total);

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

        return allUpdated && invoiceDAO.update(hd);
    }

    public CheckoutTotals previewTotals(Invoice hd, List<String> roomCodes, String maKM) {
        if (hd == null) {
            return new CheckoutTotals(0, 0, 0, 0, 0, 0);
        }

        LocalDateTime now = LocalDateTime.now();
        List<InvoiceDetail> details = invoiceDetailDAO.getByInvoice(hd.getMaHD());

        double roomFee = 0;
        double surcharge = 0;
        double lateCheckoutPenalty = 0;

        if (details != null) {
            boolean hasRoomFilter = roomCodes != null && !roomCodes.isEmpty();

            for (InvoiceDetail ct : details) {
                if (hasRoomFilter && !roomCodes.contains(ct.getMaPhong())) {
                    continue;
                }

                boolean shouldRecalculate = ct.getNgayTraThucTe() == null;

                if (shouldRecalculate) {
                    RoomCharge charge = calculateRoomCharge(hd, ct, now);

                    roomFee += charge.roomFee;
                    surcharge += charge.surcharge;
                    lateCheckoutPenalty += charge.lateCheckoutPenalty;
                } else {
                    double oldSurcharge = Math.max(0, ct.getPhuThu());
                    double oldPenalty = Math.max(0, ct.getPhiPhat());
                    double oldRoomFee = Math.max(0, ct.getThanhTien() - oldSurcharge - oldPenalty);

                    roomFee += oldRoomFee;
                    surcharge += oldSurcharge;
                    lateCheckoutPenalty += oldPenalty;
                }
            }
        }

        List<ServiceDetail> services = serviceDetailDAO.getByInvoice(hd.getMaHD());
        double serviceFee = 0;

        if (services != null) {
            for (ServiceDetail sd : services) {
                serviceFee += sd.getThanhTien();
            }
        }

        double amountBeforeTax = roomFee + surcharge + serviceFee + lateCheckoutPenalty;

        double tax = amountBeforeTax * 0.10;

        double amountBeforeDiscount = amountBeforeTax + tax;

        // 1. Giảm theo mã khuyến mãi trước
        double discount = calculatePromotionDiscount(maKM, amountBeforeDiscount);

        // 2. Giảm theo hạng khách hàng sau khi đã trừ mã khuyến mãi
        double rankDiscountRate = getCustomerRankDiscountRate(hd.getMaKhachHang());
        double amountAfterPromotion = Math.max(0, amountBeforeDiscount - discount);
        double rankDiscount = amountAfterPromotion * rankDiscountRate;

        // 3. Tổng cuối
        double total = Math.max(0, amountBeforeDiscount - discount - rankDiscount);

        return new CheckoutTotals(
                roomFee,
                serviceFee,
                surcharge,
                tax,
                discount,
                total,
                lateCheckoutPenalty,
                rankDiscount,
                rankDiscountRate
        );
    }

    public double getRemainingDepositForCheckout(Invoice hd, List<String> currentRoomCodes) {
        if (hd == null || hd.getMaDatPhong() == null || hd.getMaDatPhong().isBlank()) {
            return 0;
        }

        double originalDeposit = invoiceDAO.getDepositAmount(hd.getMaDatPhong());

        if (originalDeposit <= 0) {
            return 0;
        }

        double usedDeposit = calculateDepositUsedByPreviousCheckedOutRooms(hd, currentRoomCodes);

        return Math.max(0, originalDeposit - usedDeposit);
    }

    private double calculateDepositUsedByPreviousCheckedOutRooms(Invoice hd, List<String> currentRoomCodes) {
        if (hd == null || hd.getMaHD() == null || hd.getMaHD().isBlank()) {
            return 0;
        }

        List<InvoiceDetail> details = invoiceDetailDAO.getByInvoice(hd.getMaHD());

        if (details == null || details.isEmpty()) {
            return 0;
        }

        double used = 0;

        for (InvoiceDetail ct : details) {
            if (ct == null) {
                continue;
            }

            // Chỉ tính các phòng đã trả trước đó.
            if (ct.getNgayTraThucTe() == null) {
                continue;
            }

            // Không tính phòng đang thanh toán hiện tại.
            if (currentRoomCodes != null && currentRoomCodes.contains(ct.getMaPhong())) {
                continue;
            }

            double surcharge = Math.max(0, ct.getPhuThu());
            double penalty = Math.max(0, ct.getPhiPhat());
            double roomFee = Math.max(0, ct.getThanhTien() - surcharge - penalty);

            double amountBeforeTax = roomFee + surcharge + penalty;
            double tax = amountBeforeTax * 0.10;

            used += amountBeforeTax + tax;
        }

        return used;
    }

    private void recalculateInvoiceTotals(Invoice hd, String maKM) {
        CheckoutTotals totals = previewTotals(hd, null, maKM);

        hd.setMaKhuyenMai((maKM == null || maKM.isBlank()) ? null : maKM);
        hd.setTienPhong(totals.roomFee);
        hd.setTienDichVu(totals.serviceFee);
        hd.setTienThue(totals.tax);
        hd.setTienKhuyenMai(totals.totalDiscount);
        hd.setTongTienThanhToan(totals.total);
    }

    private RoomCharge calculateRoomCharge(Invoice hd, InvoiceDetail ct, LocalDateTime actualOut) {
        double pricePerNight = getBookingRoomPrice(hd.getMaDatPhong(), ct.getMaPhong());

        if (pricePerNight <= 0) {
            Room room = roomDAO.getById(ct.getMaPhong());
            if (room == null) {
                return new RoomCharge(1, 0, 0, 0, 0);
            }

            RoomType roomType = roomTypeDAO.getById(room.getLoaiPhong().getMaLoaiPhong());
            if (roomType == null) {
                return new RoomCharge(1, 0, 0, 0, 0);
            }

            pricePerNight = roomType.getGiaPhong();
        }

        LocalDateTime expectedIn = getExpectedCheckinTime(hd.getMaDatPhong(), ct.getMaPhong());
        LocalDateTime expectedOut = ct.getNgayTraPhong();
        LocalDateTime actualIn = ct.getNgayNhanPhong();

        if (actualIn == null) actualIn = expectedIn;
        if (actualIn == null) actualIn = LocalDateTime.now();
        if (actualOut == null) actualOut = LocalDateTime.now();

        int actualNights = ct.getSoDem();
        if (actualNights < 1) {
            actualNights = 1;
        }

        if (expectedOut != null && actualOut != null && actualOut.isAfter(expectedOut)) {
            long lateDays = ChronoUnit.DAYS.between(
                    expectedOut.toLocalDate(),
                    actualOut.toLocalDate()
            );

            if (lateDays > 0) {
                actualNights += (int) lateDays;
            }

            int expectedMinutes = expectedOut.getHour() * 60 + expectedOut.getMinute();
            int actualMinutesOfDay = actualOut.getHour() * 60 + actualOut.getMinute();

            int lateMinutesInDay = actualMinutesOfDay - expectedMinutes;

            if (lateMinutesInDay > 6 * 60) {
                actualNights += 1;
            }
        }

        /*
         * Nhận phòng quá sớm:
         * Nếu khách nhận trước ngày/giờ dự kiến, cùng ngày dự kiến và trước 5h sáng
         * thì tính thêm 1 đêm vào tiền phòng.
         */
        if (expectedIn != null && actualIn.isBefore(expectedIn)) {
            if (!actualIn.toLocalDate().isEqual(expectedIn.toLocalDate())) {
                long earlyDays = ChronoUnit.DAYS.between(
                        actualIn.toLocalDate(),
                        expectedIn.toLocalDate()
                );

                if (earlyDays < 1) {
                    earlyDays = 1;
                }

                actualNights += (int) earlyDays;
            } else if (actualIn.getHour() < 5) {
                actualNights += 1;
            }
        }

        double roomFee = actualNights * pricePerNight;

        double surcharge = 0;

        // Phụ thu nhận sớm 5h-11h: 50%, 11h-14h: 25%, <5h đã cộng thêm 1 đêm nên không phụ thu nữa
        surcharge += calculateEarlyCheckinFee(expectedIn, actualIn, pricePerNight);

        double taxableRoomFee = roomFee;

        double lateCheckoutPenalty = calculateLateCheckoutPenalty(expectedOut, actualOut, pricePerNight);

        return new RoomCharge(actualNights, roomFee, surcharge, taxableRoomFee, lateCheckoutPenalty);
    }

    private double getBookingRoomPrice(String maDatPhong, String maPhong) {
        if (maDatPhong == null || maDatPhong.isBlank() || maPhong == null || maPhong.isBlank()) {
            return 0;
        }

        String sql = "SELECT donGiaDat FROM ChiTietDatPhong WHERE maDatPhong = ? AND maPhong = ?";

        try {
            java.sql.Connection con = kqlhotel.dao.ConnectDB.getInstance().getConnection();
            java.sql.PreparedStatement ps = con.prepareStatement(sql);

            ps.setString(1, maDatPhong);
            ps.setString(2, maPhong);

            java.sql.ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getDouble("donGiaDat");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    private LocalDateTime getExpectedCheckinTime(String maDatPhong, String maPhong) {
        if (maDatPhong == null || maDatPhong.isBlank() || maPhong == null || maPhong.isBlank()) {
            return null;
        }

        String sql = "SELECT ngayNhanDuKien FROM ChiTietDatPhong WHERE maDatPhong = ? AND maPhong = ?";

        try {
            java.sql.Connection con = kqlhotel.dao.ConnectDB.getInstance().getConnection();
            java.sql.PreparedStatement ps = con.prepareStatement(sql);

            ps.setString(1, maDatPhong);
            ps.setString(2, maPhong);

            java.sql.ResultSet rs = ps.executeQuery();
            if (rs.next() && rs.getTimestamp("ngayNhanDuKien") != null) {
                return rs.getTimestamp("ngayNhanDuKien").toLocalDateTime();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private double calculateEarlyCheckinFee(LocalDateTime expectedIn,
                                            LocalDateTime actualIn,
                                            double pricePerNight) {
        if (expectedIn == null || actualIn == null) return 0;

        // Không đến sớm thì không phụ thu
        if (!actualIn.isBefore(expectedIn)) return 0;

        if (!actualIn.toLocalDate().isEqual(expectedIn.toLocalDate())) {
            return 0;
        }

        int hour = actualIn.getHour();

        // Cùng ngày, trước 5h: đã cộng thêm 1 đêm trong calculateRoomCharge()
        if (hour < 5) {
            return 0;
        }

        // Cùng ngày, 5h-11h: phụ thu 50%
        if (hour < 11) {
            return pricePerNight * 0.50;
        }

        // Cùng ngày, 11h-14h: phụ thu 25%
        if (hour < 14) {
            return pricePerNight * 0.25;
        }

        return 0;
    }

    private double calculateLateCheckoutPenalty(LocalDateTime expectedOut,
                                                LocalDateTime actualOut,
                                                double pricePerNight) {
        if (expectedOut == null || actualOut == null) {
            return 0;
        }

        if (!actualOut.isAfter(expectedOut)) {
            return 0;
        }

        int expectedMinutes = expectedOut.getHour() * 60 + expectedOut.getMinute();
        int actualMinutes = actualOut.getHour() * 60 + actualOut.getMinute();

        int lateMinutesInDay = actualMinutes - expectedMinutes;

        // Trả trước hoặc đúng giờ trong ngày cuối
        if (lateMinutesInDay <= 0) {
            return 0;
        }

        // Trễ từ 1 tiếng trở xuống: không phạt
        if (lateMinutesInDay <= 60) {
            return 0;
        }

        // Trễ hơn 6 tiếng: đã cộng thêm 1 đêm ở calculateRoomCharge()
        if (lateMinutesInDay > 6 * 60) {
            return 0;
        }

        // Trễ hơn 1 tiếng và trong vòng 6 tiếng: phạt 50% giá 1 đêm
        return pricePerNight * 0.50;
    }

    private double calculatePromotionDiscount(String maKM, double amountBeforeDiscount) {
        if (maKM == null || maKM.isBlank()) {
            return 0;
        }

        Promotion km = promotionDAO.getById(maKM);
        if (km == null) {
            return 0;
        }

        // Không đủ điều kiện thì không giảm
        if (!isPromotionEligible(km, amountBeforeDiscount)) {
            return 0;
        }

        double discount;

        if ("TheoPhanTram".equals(km.getLoaiKM())) {
            discount = amountBeforeDiscount * km.getTienKhuyenMai() / 100.0;

            if (km.getGiaTriToiDa() > 0) {
                discount = Math.min(discount, km.getGiaTriToiDa());
            }
        } else {
            discount = km.getTienKhuyenMai();
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
                            "ctdp.ngayNhanDuKien, ctdp.ngayTraDuKien, " +
                            "cthd.ngayNhanPhong, cthd.ngayTraPhong, cthd.ngayTraThucTe, " +
                            "lp.giaPhong " +
                            "FROM HoaDon hd " +
                            "JOIN DatPhong dp ON hd.maDatPhong = dp.maDatPhong " +
                            "JOIN ChiTietDatPhong ctdp ON dp.maDatPhong = ctdp.maDatPhong " +
                            "JOIN ChiTietHoaDon cthd ON hd.maHD = cthd.maHD AND ctdp.maPhong = cthd.maPhong " +
                            "JOIN Phong p ON ctdp.maPhong = p.maPhong " +
                            "JOIN LoaiPhong lp ON p.maLoaiPhong = lp.maLoaiPhong " +
                            "JOIN KhachHang kh ON hd.maKH = kh.maKH " +
                            "WHERE hd.trangThai = 'ChuaThanhToan' " +
                            "AND cthd.ngayNhanPhong IS NOT NULL " +
                            "AND cthd.ngayTraThucTe IS NULL "
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

            sql.append("ORDER BY ctdp.ngayTraDuKien ASC, hd.maHD ASC");

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
                list.add(mapCheckoutData(rs, "Đang ở", new java.awt.Color(240, 60, 60)));
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
                            "ctdp.ngayNhanDuKien, ctdp.ngayTraDuKien, " +
                            "cthd.ngayNhanPhong, cthd.ngayTraPhong, cthd.ngayTraThucTe, " +
                            "lp.giaPhong " +
                            "FROM HoaDon hd " +
                            "JOIN DatPhong dp ON hd.maDatPhong = dp.maDatPhong " +
                            "JOIN ChiTietDatPhong ctdp ON dp.maDatPhong = ctdp.maDatPhong " +
                            "JOIN ChiTietHoaDon cthd ON hd.maHD = cthd.maHD AND ctdp.maPhong = cthd.maPhong " +
                            "JOIN Phong p ON ctdp.maPhong = p.maPhong " +
                            "JOIN LoaiPhong lp ON p.maLoaiPhong = lp.maLoaiPhong " +
                            "JOIN KhachHang kh ON hd.maKH = kh.maKH " +
                            "WHERE hd.trangThai = 'ChuaThanhToan' " +
                            "AND cthd.ngayNhanPhong IS NOT NULL " +
                            "AND cthd.ngayTraThucTe IS NULL " +
                            "AND CAST(ctdp.ngayTraDuKien AS DATE) = CAST(GETDATE() AS DATE) " +
                            "ORDER BY ctdp.ngayTraDuKien ASC, hd.maHD ASC";

            java.sql.PreparedStatement pstmt = con.prepareStatement(sql);
            java.sql.ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                list.add(mapCheckoutData(rs, "Trả hôm nay", new java.awt.Color(240, 60, 60)));
            }
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    private kqlhotel.gui.tabs.CheckoutPanel.CheckoutData mapCheckoutData(
            java.sql.ResultSet rs,
            String statusText,
            java.awt.Color statusColor
    ) throws java.sql.SQLException {
        String id = rs.getString("maHD");
        String rName = "Phòng " + rs.getString("maPhong") + " · " + rs.getString("tenLoaiPhong");
        String cName = rs.getString("hoTenKH");
        String phone = rs.getString("sdt");

        String expectedIn = kqlhotel.utils.DateUtils.format(rs.getTimestamp("ngayNhanDuKien").toLocalDateTime());
        String expectedOut = kqlhotel.utils.DateUtils.format(rs.getTimestamp("ngayTraDuKien").toLocalDateTime());
        String actualIn = kqlhotel.utils.DateUtils.format(rs.getTimestamp("ngayNhanPhong").toLocalDateTime());
        String actualOut = "Chưa trả";

        String price = kqlhotel.utils.CurrencyUtils.formatVND(rs.getDouble("giaPhong")) + "/đêm";

        return new kqlhotel.gui.tabs.CheckoutPanel.CheckoutData(
                id, rName, cName, phone,
                expectedIn, expectedOut,
                actualIn, actualOut,
                price, statusText, statusColor
        );
    }

    private Invoice getActiveByRoomFromBooking(String maPhong) {
        try {
            java.sql.Connection con = kqlhotel.dao.ConnectDB.getInstance().getConnection();

            String sql =
                    "SELECT TOP 1 hd.* " +
                            "FROM HoaDon hd " +
                            "JOIN ChiTietDatPhong ctdp ON hd.maDatPhong = ctdp.maDatPhong " +
                            "JOIN ChiTietHoaDon cthd ON hd.maHD = cthd.maHD AND ctdp.maPhong = cthd.maPhong " +
                            "WHERE ctdp.maPhong = ? " +
                            "AND hd.trangThai = 'ChuaThanhToan' " +
                            "AND cthd.ngayNhanPhong IS NOT NULL " +
                            "AND cthd.ngayTraThucTe IS NULL " +
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

    public double getCurrentSurcharge(Invoice hd) {
        if (hd == null) {
            return 0;
        }

        List<InvoiceDetail> details = invoiceDetailDAO.getByInvoice(hd.getMaHD());
        double total = 0;

        if (details != null) {
            for (InvoiceDetail ct : details) {
                total += Math.max(0, ct.getPhuThu());
            }
        }

        return total;
    }

    public double previewSurcharge(Invoice hd, List<String> roomCodes) {
        return previewTotals(hd, roomCodes, null).surcharge;
    }
    private boolean isPromotionEligible(Promotion km, double amountBeforeDiscount) {
        if (km == null) return false;

        double minAmount = km.getDieuKienApDung();

        return amountBeforeDiscount > minAmount;
    }

    private double parseMinimumAmount(String condition) {
        if (condition == null || condition.isBlank()) {
            return 0;
        }

        try {
            return Double.parseDouble(condition.trim().replace(",", ""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public boolean createCheckoutPayment(Invoice hd, double amount, String method, String maNV) {
        if (hd == null || amount <= 0) {
            return true;
        }

        String sqlMax = "SELECT MAX(maTT) AS maxMaTT FROM ThanhToan";
        String sqlInsert = """
        INSERT INTO ThanhToan
        (maTT, ngayTT, soTienTT, ghiChu, phuongThucTT, trangThaiTT, maHD, maPC, maNV)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
    """;

        try {
            java.sql.Connection con = kqlhotel.dao.ConnectDB.getInstance().getConnection();

            String newMaTT = "TT001";
            try (java.sql.PreparedStatement ps = con.prepareStatement(sqlMax);
                 java.sql.ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getString("maxMaTT") != null) {
                    String max = rs.getString("maxMaTT"); // VD: TT055
                    int num = Integer.parseInt(max.substring(2)) + 1;
                    newMaTT = String.format("TT%03d", num);
                }
            }

            try (java.sql.PreparedStatement ps = con.prepareStatement(sqlInsert)) {
                ps.setString(1, newMaTT);
                ps.setTimestamp(2, java.sql.Timestamp.valueOf(java.time.LocalDateTime.now()));
                ps.setDouble(3, amount);
                ps.setString(4, "Thanh toan tra phong");
                ps.setString(5, method);
                ps.setString(6, "ThanhToanThanhCong");
                ps.setString(7, hd.getMaHD());
                ps.setString(8, "PC005");
                ps.setString(9, maNV);

                return ps.executeUpdate() > 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public String getCustomerRank(String maKH) {
        if (maKH == null || maKH.isBlank()) {
            return "Dong";
        }

        String sql = "SELECT hangKH FROM KhachHang WHERE maKH = ?";

        try {
            java.sql.Connection con = kqlhotel.dao.ConnectDB.getInstance().getConnection();
            java.sql.PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, maKH);

            java.sql.ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String rank = rs.getString("hangKH");
                return rank == null || rank.isBlank() ? "Dong" : rank;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "Dong";
    }

    public String getCustomerRankDisplay(String maKH) {
        String rank = getCustomerRank(maKH);

        return switch (rank) {
            case "Bac" -> "Bạc";
            case "Vang" -> "Vàng";
            case "KimCuong" -> "Kim cương";
            default -> "Đồng";
        };
    }

    public double getCustomerRankDiscountRate(String maKH) {
        String rank = getCustomerRank(maKH);

        return switch (rank) {
            case "Bac" -> 0.05;
            case "Vang" -> 0.10;
            case "KimCuong" -> 0.15;
            default -> 0.0;
        };
    }

    public int addCustomerLoyaltyPoints(String maKH, double spentAmount) {
        if (maKH == null || maKH.isBlank()) {
            return 0;
        }

        int points = (int) Math.floor(Math.max(0, spentAmount) / 1_000_000.0);

        if (points <= 0) {
            return 0;
        }

        String sql = """
        UPDATE KhachHang
        SET diemTichLuy = diemTichLuy + ?
        WHERE maKH = ?
    """;

        try {
            java.sql.Connection con = kqlhotel.dao.ConnectDB.getInstance().getConnection();
            java.sql.PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, points);
            ps.setString(2, maKH);

            return ps.executeUpdate() > 0 ? points : 0;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }
}