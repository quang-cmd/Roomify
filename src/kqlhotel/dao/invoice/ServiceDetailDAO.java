package kqlhotel.dao.invoice;

import kqlhotel.dao.connectDB.*;

import kqlhotel.entity.ServiceDetail;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceDetailDAO {
    public List<ServiceDetail> getByInvoice(String maHD) {
        List<ServiceDetail> list = new ArrayList<>();
        try {
            Connection con = ConnectDB.getConnection();
            String sql = "SELECT * FROM ChiTietDichVu WHERE maHD = ?";
            PreparedStatement pstmt = con.prepareStatement(sql);
            pstmt.setString(1, maHD);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                ServiceDetail ct = new ServiceDetail();
                ct.setMaCTDV(rs.getString("maCTDV"));
                ct.setSoLuong(rs.getInt("soLuong"));
                ct.setDonGia(rs.getDouble("donGia"));
                ct.setThanhTien(rs.getDouble("thanhTien"));
                ct.setGhiChu(rs.getString("ghiChu"));
                ct.setMaDV(rs.getString("maDV"));
                ct.setMaHD(rs.getString("maHD"));
                list.add(ct);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
