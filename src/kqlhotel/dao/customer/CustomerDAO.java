package kqlhotel.dao.customer;

import kqlhotel.dao.connectDB.*;

import kqlhotel.entity.Customer;
import java.sql.*;

public class CustomerDAO {
    public Customer getById(String id) {
        Customer customer = null;
        try {
            Connection con = ConnectDB.getConnection();
            String sql = "SELECT * FROM KhachHang WHERE maKH = ?";
            PreparedStatement pstmt = con.prepareStatement(sql);
            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                customer = new Customer();
                customer.setMaKH(rs.getString("maKH"));
                customer.setHoTenKH(rs.getString("hoTenKH"));
                customer.setSdt(rs.getString("sdt"));
                customer.setHangKH(rs.getString("hangKH"));
                // ... others
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return customer;
    }
}
