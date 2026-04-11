package kqlhotel.dao;

import kqlhotel.entity.InvoiceDetail;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InvoiceDetailDAO {
    public List<InvoiceDetail> getByInvoice(String maHD) {
        List<InvoiceDetail> list = new ArrayList<>();
        try {
            Connection con = ConnectDB.getConnection();
            String sql = "SELECT * FROM ChiTietHoaDon WHERE maHD = ?";
            PreparedStatement pstmt = con.prepareStatement(sql);
            pstmt.setString(1, maHD);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                InvoiceDetail ct = new InvoiceDetail();
                ct.setMaCTHD(rs.getString("maCTHD"));
                ct.setNgayNhanPhong(rs.getTimestamp("ngayNhanPhong").toLocalDateTime());
                ct.setNgayTraPhong(rs.getTimestamp("ngayTraPhong").toLocalDateTime());
                ct.setSoDem(rs.getInt("soDem"));
                ct.setPhuThu(rs.getDouble("phuThu"));
                ct.setThanhTien(rs.getDouble("thanhTien"));
                ct.setMaPhong(rs.getString("phong"));
                ct.setMaHD(rs.getString("maHD"));
                list.add(ct);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
