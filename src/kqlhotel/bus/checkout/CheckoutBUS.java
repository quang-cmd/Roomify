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
import java.time.Duration;

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
        public double discount;
        public double total;

        public CheckoutTotals(double roomFee, double serviceFee, double surcharge, double tax, double discount, double total) {
            this.roomFee = roomFee;
            this.serviceFee = serviceFee;
            this.surcharge = surcharge;
            this.tax = tax;
            this.discount = discount;
            this.total = total;
        }
    }

    private static class RoomCharge {
        int nights;
        double roomFee;
        double surcharge;
        double total;

        RoomCharge(int nights, double roomFee, double surcharge) {
            this.nights = nights;
            this.roomFee = roomFee;
            this.surcharge = surcharge;
            this.total = roomFee + surcharge;
        }
    }

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

        CheckoutTotals totals = previewTotals(hd, null, maKM);

        hd.setMaKhuyenMai((maKM == null || maKM.isBlank()) ? null : maKM);
        hd.setTienPhong(totals.roomFee + totals.surcharge);
        hd.setTienDichVu(totals.serviceFee);
        hd.setTienThue(totals.tax);
        hd.setTienKhuyenMai(totals.discount);
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
        hd.setTienPhong(totals.roomFee + totals.surcharge);
        hd.setTienDichVu(totals.serviceFee);
        hd.setTienThue(totals.tax);
        hd.setTienKhuyenMai(totals.discount);
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

        if (details != null) {
            for (InvoiceDetail ct : details) {
                boolean shouldRecalculate =
                        ct.getNgayTraThucTe() == null
                                && (roomCodes == null || roomCodes.contains(ct.getMaPhong()));

                if (shouldRecalculate) {
                    RoomCharge charge = calculateRoomCharge(hd, ct, now);
                    roomFee += charge.roomFee;
                    surcharge += charge.surcharge;
                } else {
                    double oldSurcharge = Math.max(0, ct.getPhuThu());
                    double oldRoomFee = Math.max(0, ct.getThanhTien() - oldSurcharge);

                    roomFee += oldRoomFee;
                    surcharge += oldSurcharge;
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

        double subTotal = roomFee + serviceFee + surcharge;
        double tax = subTotal * 0.10;
        double discount = calculatePromotionDiscount(maKM, subTotal + tax);
        double total = Math.max(0, subTotal + tax - discount);

        return new CheckoutTotals(roomFee, serviceFee, surcharge, tax, discount, total);
    }

    private void recalculateInvoiceTotals(Invoice hd, String maKM) {
        CheckoutTotals totals = previewTotals(hd, null, maKM);

        hd.setMaKhuyenMai((maKM == null || maKM.isBlank()) ? null : maKM);
        hd.setTienPhong(totals.roomFee + totals.surcharge);
        hd.setTienDichVu(totals.serviceFee);
        hd.setTienThue(totals.tax);
        hd.setTienKhuyenMai(totals.discount);
        hd.setTongTienThanhToan(totals.total);
    }

    private RoomCharge calculateRoomCharge(Invoice hd, InvoiceDetail ct, LocalDateTime actualOut) {
        // price = đơn giá 1 đêm của phòng
        double pricePerNight = getBookingRoomPrice(hd.getMaDatPhong(), ct.getMaPhong());

        if (pricePerNight <= 0) {
            Room room = roomDAO.getById(ct.getMaPhong());
            if (room == null) {
                return new RoomCharge(1, 0, 0);
            }

            RoomType roomType = roomTypeDAO.getById(room.getLoaiPhong());
            if (roomType == null) {
                return new RoomCharge(1, 0, 0);
            }

            pricePerNight = roomType.getGiaPhong();
        }

        LocalDateTime expectedIn = getExpectedCheckinTime(hd.getMaDatPhong(), ct.getMaPhong());
        LocalDateTime expectedOut = ct.getNgayTraPhong();
        LocalDateTime actualIn = ct.getNgayNhanPhong();

        if (actualIn == null) actualIn = expectedIn;
        if (actualIn == null) actualIn = LocalDateTime.now();

        // ===== THỜI GIAN DỰ KIẾN =====
        long expectedHours = 0;
        if (expectedIn != null && expectedOut != null) {
            expectedHours = Duration.between(expectedIn, expectedOut).toHours();
        }

        if (expectedHours <= 0) expectedHours = 24;

        double expectedDays = expectedHours / 24.0;
        long expectedNights = (long) Math.ceil(expectedDays);
        if (expectedNights <= 0) expectedNights = 1;

        // ===== THỜI GIAN THỰC TẾ =====
        long actualHours = Duration.between(actualIn, actualOut).toHours();
        if (actualHours <= 0) actualHours = 1;

        double actualDays = actualHours / 24.0;
        int actualNightsForSave = (int) Math.ceil(actualDays);
        if (actualNightsForSave <= 0) actualNightsForSave = 1;

        // ===== TIỀN PHÒNG GỐC =====
        double originalRoomFee = expectedNights * pricePerNight;

        // ===== PHỤ THU TÍNH THEO GIÁ 1 ĐÊM =====
        double earlyCheckinFee = calculateEarlyCheckinFee(expectedIn, actualIn, pricePerNight);
        double lateCheckoutFee = calculateLateCheckoutFee(expectedOut, actualOut, pricePerNight);

        // ===== GIẢM DO TRẢ SỚM =====
        double earlyCheckoutDiscount = 0;

        if (expectedOut != null && actualOut.isBefore(expectedOut)) {
            long earlyHours = Duration.between(actualOut, expectedOut).toHours();

            if (earlyHours >= 24) {
                if (actualDays <= expectedDays * 0.5) {
                    earlyCheckoutDiscount = originalRoomFee * 0.50;
                } else {
                    earlyCheckoutDiscount = originalRoomFee * 0.30;
                }

                // Hoàn tiền trả sớm tối đa 50% tổng tiền phòng
                double maxEarlyRefund = originalRoomFee * 0.50;
                earlyCheckoutDiscount = Math.min(earlyCheckoutDiscount, maxEarlyRefund);
            }
        }

        // Tiền phòng = tiền phòng dự kiến - giảm trả sớm
        double roomFee = Math.max(0, originalRoomFee - earlyCheckoutDiscount);

        // Phụ thu = phụ thu nhận sớm + phụ thu trả trễ
        double surcharge = earlyCheckinFee + lateCheckoutFee;

        return new RoomCharge(actualNightsForSave, roomFee, surcharge);
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
                return rs.getDouble("donGiaDat"); // giá 1 đêm
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

    private double calculateEarlyCheckinFee(LocalDateTime expectedIn, LocalDateTime actualIn, double pricePerNight) {
        if (expectedIn == null || actualIn == null) return 0;

        if (!actualIn.isBefore(expectedIn)) return 0;

        long minutes = Duration.between(actualIn, expectedIn).toMinutes();

        if (minutes <= 120) return pricePerNight * 0.10;
        if (minutes <= 360) return pricePerNight * 0.30;

        return pricePerNight * 0.50;
    }

    private double calculateLateCheckoutFee(LocalDateTime expectedOut, LocalDateTime actualOut, double pricePerNight) {
        if (expectedOut == null || actualOut == null) return 0;

        if (!actualOut.isAfter(expectedOut)) return 0;

        long minutes = Duration.between(expectedOut, actualOut).toMinutes();

        if (minutes <= 120) return pricePerNight * 0.10;
        if (minutes <= 360) return pricePerNight * 0.30;

        long lateDays = ChronoUnit.DAYS.between(
                expectedOut.toLocalDate(),
                actualOut.toLocalDate()
        );

        if (lateDays <= 0) lateDays = 1;

        return lateDays * pricePerNight;
    }

    private double calculatePromotionDiscount(String maKM, double amountBeforeDiscount) {
        if (maKM == null || maKM.isBlank()) {
            return 0;
        }

        Promotion km = promotionDAO.getById(maKM);
        if (km == null) {
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
}