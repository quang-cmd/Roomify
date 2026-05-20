import java.sql.*;

public class DBCheck {
    public static void main(String[] args) {
        String url = "jdbc:sqlserver://localhost:1433;databaseName=QLKhachSan;encrypt=false;trustServerCertificate=true";
        String user = "sa";
        String password = "123456";
        try (Connection con = DriverManager.getConnection(url, user, password)) {
            System.out.println("Connected to database successfully!");

            System.out.println("\n--- DANG MO SHIFTS (PhanCongCa where trangThai = N'DangMo') ---");
            String sqlShifts = "SELECT maPC, ngay, tienMoCa, trangThai, maNV, maCa FROM PhanCongCa WHERE trangThai = N'DangMo'";
            try (Statement stmt = con.createStatement(); ResultSet rs = stmt.executeQuery(sqlShifts)) {
                boolean found = false;
                while (rs.next()) {
                    found = true;
                    System.out.printf("maPC: %s, ngay: %s, tienMoCa: %.2f, trangThai: %s, maNV: %s, maCa: %s\n",
                            rs.getString("maPC"),
                            rs.getTimestamp("ngay"),
                            rs.getBigDecimal("tienMoCa"),
                            rs.getString("trangThai"),
                            rs.getString("maNV"),
                            rs.getString("maCa"));
                }
                if (!found) {
                    System.out.println("No open shifts found!");
                }
            }

            System.out.println("\n--- ALL SHIFTS (PhanCongCa, top 5) ---");
            String sqlAllShifts = "SELECT TOP 5 maPC, ngay, tienMoCa, trangThai, maNV, maCa FROM PhanCongCa ORDER BY ngay DESC, maPC DESC";
            try (Statement stmt = con.createStatement(); ResultSet rs = stmt.executeQuery(sqlAllShifts)) {
                while (rs.next()) {
                    System.out.printf("maPC: %s, ngay: %s, tienMoCa: %.2f, trangThai: %s, maNV: %s, maCa: %s\n",
                            rs.getString("maPC"),
                            rs.getTimestamp("ngay"),
                            rs.getBigDecimal("tienMoCa"),
                            rs.getString("trangThai"),
                            rs.getString("maNV"),
                            rs.getString("maCa"));
                }
            }

            System.out.println("\n--- LAST 5 TRANSACTIONS (ThanhToan) ---");
            String sqlPayments = "SELECT TOP 5 maTT, ngayTT, soTienTT, ghiChu, phuongThucTT, trangThaiTT, maHD, maPC, maNV, loaiGD FROM ThanhToan ORDER BY ngayTT DESC, maTT DESC";
            try (Statement stmt = con.createStatement(); ResultSet rs = stmt.executeQuery(sqlPayments)) {
                while (rs.next()) {
                    System.out.printf("maTT: %s, ngayTT: %s, soTien: %.2f, ghiChu: %s, loai: %s, maPC: %s, maNV: %s\n",
                            rs.getString("maTT"),
                            rs.getTimestamp("ngayTT"),
                            rs.getDouble("soTienTT"),
                            rs.getString("ghiChu"),
                            rs.getString("loaiGD"),
                            rs.getString("maPC"),
                            rs.getString("maNV"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
