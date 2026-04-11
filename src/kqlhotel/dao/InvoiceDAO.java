package kqlhotel.dao;

import kqlhotel.entity.Invoice;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InvoiceDAO {
    public List<Invoice> getAll() {
        List<Invoice> list = new ArrayList<>();
        String sql = "SELECT * FROM HoaDon";
        try (Connection con = ConnectDB.getInstance().getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRowToInvoice(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Invoice getById(String invoiceId) {
        String sql = "SELECT * FROM HoaDon WHERE maHD = ?";
        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, invoiceId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToInvoice(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean updateStatus(String invoiceId, String status) {
        String sql = "UPDATE HoaDon SET trangThai = ? WHERE maHD = ?";
        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, status);
            pstmt.setString(2, invoiceId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private Invoice mapRowToInvoice(ResultSet rs) throws SQLException {
        Invoice inv = new Invoice();
        inv.setInvoiceId(rs.getString("maHD"));
        inv.setCreatedDate(rs.getDate("ngayLapHD"));
        inv.setPaymentDate(rs.getDate("ngayThanhToan"));
        inv.setNote(rs.getString("ghiChu"));
        inv.setGuestCount(rs.getInt("soLuongNguoi"));
        inv.setRoomTotal(rs.getDouble("tienPhong"));
        inv.setServiceTotal(rs.getDouble("tienDichVu"));
        inv.setPromotionTotal(rs.getDouble("tienKhuyenMai"));
        inv.setTaxTotal(rs.getDouble("tienThue"));
        inv.setFinalTotal(rs.getDouble("tongTienThanhToan"));
        inv.setRoomChangeFee(rs.getDouble("phiDoiPhong"));
        inv.setPromotionId(rs.getString("khuyenMai"));
        inv.setCustomerId(rs.getString("khachHang"));
        inv.setStaffId(rs.getString("nhanVien"));
        inv.setPaymentMethod(rs.getString("phuongThucTT"));
        inv.setStatus(rs.getString("trangThai"));
        return inv;
    }
}
