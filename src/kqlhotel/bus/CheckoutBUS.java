package kqlhotel.bus;

import kqlhotel.dao.*;
import kqlhotel.entity.*;
import java.time.LocalDateTime;
import java.util.List;

public class CheckoutBUS {
    private InvoiceDAO invoiceDAO = new InvoiceDAO();
    private InvoiceDetailDAO invoiceDetailDAO = new InvoiceDetailDAO();
    private ServiceDetailDAO serviceDetailDAO = new ServiceDetailDAO();
    private RoomDAO roomDAO = new RoomDAO();
    private RoomTypeDAO roomTypeDAO = new RoomTypeDAO();

    public Invoice getInvoiceForCheckout(String maPhong) {
        Invoice hd = invoiceDAO.getActiveByRoom(maPhong);
        if (hd == null) return null;

        // Calculate Room Fee
        List<InvoiceDetail> chiTietRooms = invoiceDetailDAO.getByInvoice(hd.getMaHD());
        double totalRoomFee = 0;
        for (InvoiceDetail ct : chiTietRooms) {
            Room p = roomDAO.getById(ct.getMaPhong());
            if (p != null) {
                RoomType lp = roomTypeDAO.getById(p.getLoaiPhong());
                if (lp != null) {
                    // Update check-out date to now for calculations
                    ct.setNgayTraPhong(LocalDateTime.now());
                    long days = java.time.Duration.between(ct.getNgayNhanPhong(), ct.getNgayTraPhong()).toDays();
                    if (days == 0) days = 1; // Min 1 night
                    ct.setSoDem((int) days);
                    double fee = days * lp.getGiaPhong();
                    ct.setThanhTien(fee);
                    totalRoomFee += fee;
                }
            }
        }
        hd.setTienPhong(totalRoomFee);

        // Calculate Service Fee
        List<ServiceDetail> chiTietServices = serviceDetailDAO.getByInvoice(hd.getMaHD());
        double totalServiceFee = 0;
        for (ServiceDetail ct : chiTietServices) {
            totalServiceFee += ct.getThanhTien();
        }
        hd.setTienDichVu(totalServiceFee);

        // Subtotal (before tax/discount)
        double subTotal = totalRoomFee + totalServiceFee;
        
        // Mock Tax (10%)
        hd.setTienThue(subTotal * 0.1);
        
        // Final Total
        hd.setTongTienThanhToan(subTotal + hd.getTienThue() - hd.getTienKhuyenMai());

        return hd;
    }

    public boolean completeCheckout(Invoice hd, String roomCode, String nextRoomStatus) {
        // 1. Update Invoice
        hd.setNgayThanhToan(LocalDateTime.now());
        hd.setTrangThai("DaThanhToan");
        boolean updateHD = invoiceDAO.update(hd);

        // 2. Update Room Status
        boolean updateRoom = roomDAO.updateStatus(roomCode, nextRoomStatus);

        return updateHD && updateRoom;
    }

    public List<kqlhotel.gui.tabs.CheckoutPanel.CheckoutData> searchCheckoutData(String roomCode, String cusId, String cusName) {
        java.util.List<kqlhotel.gui.tabs.CheckoutPanel.CheckoutData> list = new java.util.ArrayList<>();
        try {
            java.sql.Connection con = kqlhotel.dao.ConnectDB.getConnection();
            StringBuilder sql = new StringBuilder(
                "SELECT hd.maHD, p.maPhong, lp.tenLoaiPhong, kh.hoTenKH, kh.maKH, kh.sdt, cthd.ngayNhanPhong, cthd.ngayTraPhong, lp.giaPhong " +
                "FROM HoaDon hd " +
                "JOIN ChiTietHoaDon cthd ON hd.maHD = cthd.maHD " +
                "JOIN Phong p ON cthd.phong = p.maPhong " +
                "JOIN LoaiPhong lp ON p.loaiPhong = lp.maLoaiPhong " +
                "JOIN KhachHang kh ON hd.khachHang = kh.maKH " +
                "WHERE hd.trangThai = N'ChuaThanhToan' "
            );

            if (roomCode != null && !roomCode.isEmpty()) sql.append("AND (p.maPhong LIKE ? OR hd.maHD LIKE ?) ");
            if (cusId != null && !cusId.isEmpty()) sql.append("AND kh.maKH LIKE ? ");
            if (cusName != null && !cusName.isEmpty()) sql.append("AND kh.hoTenKH LIKE ? ");

            java.sql.PreparedStatement pstmt = con.prepareStatement(sql.toString());
            int idx = 1;
            if (roomCode != null && !roomCode.isEmpty()) {
                pstmt.setString(idx++, "%" + roomCode + "%");
                pstmt.setString(idx++, "%" + roomCode + "%");
            }
            if (cusId != null && !cusId.isEmpty()) pstmt.setString(idx++, "%" + cusId + "%");
            if (cusName != null && !cusName.isEmpty()) pstmt.setString(idx++, "%" + cusName + "%");

            java.sql.ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String id = rs.getString("maHD");
                String rName = "Phòng " + rs.getString("maPhong") + " · " + rs.getString("tenLoaiPhong");
                String cName = rs.getString("hoTenKH");
                String phone = rs.getString("sdt");
                String dateIn = kqlhotel.utils.DateUtils.format(rs.getTimestamp("ngayNhanPhong").toLocalDateTime());
                String dateOut = kqlhotel.utils.DateUtils.format(rs.getTimestamp("ngayTraPhong").toLocalDateTime());
                String price = kqlhotel.utils.CurrencyUtils.formatVND(rs.getDouble("giaPhong")) + "/đêm";
                
                list.add(new kqlhotel.gui.tabs.CheckoutPanel.CheckoutData(
                    id, rName, cName, phone, dateIn, dateOut, price, "Đang ở", new java.awt.Color(240, 60, 60)
                ));
            }
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<kqlhotel.gui.tabs.CheckoutPanel.CheckoutData> getRoomsDueToday() {
        java.util.List<kqlhotel.gui.tabs.CheckoutPanel.CheckoutData> list = new java.util.ArrayList<>();
        try {
            java.sql.Connection con = kqlhotel.dao.ConnectDB.getConnection();
            String sql = "SELECT hd.maHD, p.maPhong, lp.tenLoaiPhong, kh.hoTenKH, kh.maKH, kh.sdt, cthd.ngayNhanPhong, cthd.ngayTraPhong, lp.giaPhong " +
                         "FROM HoaDon hd " +
                         "JOIN ChiTietHoaDon cthd ON hd.maHD = cthd.maHD " +
                         "JOIN Phong p ON cthd.phong = p.maPhong " +
                         "JOIN LoaiPhong lp ON p.loaiPhong = lp.maLoaiPhong " +
                         "JOIN KhachHang kh ON hd.khachHang = kh.maKH " +
                         "WHERE hd.trangThai = N'ChuaThanhToan' " +
                         "AND CAST(cthd.ngayTraPhong AS DATE) = CAST(GETDATE() AS DATE)";

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
                
                list.add(new kqlhotel.gui.tabs.CheckoutPanel.CheckoutData(
                    id, rName, cName, phone, dateIn, dateOut, price, "Trả hôm nay", new java.awt.Color(240, 60, 60)
                ));
            }
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
