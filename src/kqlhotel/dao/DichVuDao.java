package kqlhotel.dao;

import kqlhotel.entity.DichVuEntity;
import kqlhotel.dao.ConnectDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DichVuDao {

    public List<DichVuEntity> getAll() {
        List<DichVuEntity> list = new ArrayList<>();
        String sql = "SELECT * FROM dichvu";

        try (Connection conn = ConnectDB.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                DichVuEntity dv = new DichVuEntity(
                        rs.getString("ma_dv"),
                        rs.getString("ten_dv"),
                        rs.getDouble("gia"),
                        rs.getString("loai_dv"),
                        rs.getString("mo_ta"),
                        rs.getString("trang_thai")
                );
                list.add(dv);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}