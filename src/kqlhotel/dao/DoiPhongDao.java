package kqlhotel.dao;

import kqlhotel.entity.PhongEntity;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DoiPhongDao {

    private Connection conn;

    public DoiPhongDao(Connection conn) {
        this.conn = conn;
    }

    // Lấy danh sách phòng
    public List<PhongEntity> getDanhSachPhong() {
        List<PhongEntity> list = new ArrayList<>();

        String sql = "SELECT ma_phong, trang_thai FROM Phong"; // ⚠️ sửa nếu khác DB

        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                PhongEntity p = new PhongEntity(
                        rs.getString("ma_phong"),
                        rs.getString("trang_thai")
                );
                list.add(p);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    // Đổi phòng
    public void doiPhong(String phongCu, String phongMoi) {
        try {
            conn.setAutoCommit(false);

            String sql1 = "UPDATE Phong SET trang_thai = N'Trống' WHERE ma_phong = ?";
            String sql2 = "UPDATE Phong SET trang_thai = N'Đang ở' WHERE ma_phong = ?";

            PreparedStatement ps1 = conn.prepareStatement(sql1);
            ps1.setString(1, phongCu);
            ps1.executeUpdate();

            PreparedStatement ps2 = conn.prepareStatement(sql2);
            ps2.setString(1, phongMoi);
            ps2.executeUpdate();

            conn.commit();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}