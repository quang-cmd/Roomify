package kqlhotel.dao.service;

import kqlhotel.dao.ConnectDB;
import kqlhotel.entity.Service;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceDAO {

    public List<Service> getAllActive() {
        List<Service> list = new ArrayList<>();
        try {
            Connection con = ConnectDB.getInstance().getConnection();
            String sql = "SELECT * FROM DichVu WHERE trangThaiDV = 'DangHoatDong' ORDER BY tenDV";
            PreparedStatement pstmt = con.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Service s = new Service();
                s.setMaDV(rs.getString("maDV"));
                s.setTenDV(rs.getString("tenDV"));
                s.setDonGia(rs.getDouble("donGia"));
                s.setMoTaDV(rs.getString("moTaDV"));
                s.setTrangThaiDV(rs.getString("trangThaiDV"));
                list.add(s);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
