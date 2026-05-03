package kqlhotel.scratch;

import kqlhotel.dao.ConnectDB;
import java.sql.*;

public class CheckSchema {
    public static void main(String[] args) {
        try (Connection con = ConnectDB.getConnection()) {
            DatabaseMetaData metaData = con.getMetaData();
            ResultSet columns = metaData.getColumns(null, null, "KhachHang", null);
            
            System.out.println("Cac cot trong bang KhachHang:");
            boolean foundHangKH = false;
            while (columns.next()) {
                String columnName = columns.getString("COLUMN_NAME");
                System.out.println("- " + columnName);
                if ("hangKH".equalsIgnoreCase(columnName)) {
                    foundHangKH = true;
                }
            }
            
            if (foundHangKH) {
                System.out.println("\n=> Ket luan: Co cot 'hangKH' (Hang khach hang).");
            } else {
                System.out.println("\n=> Ket luan: KHONG TIM THAY cot 'hangKH'.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
