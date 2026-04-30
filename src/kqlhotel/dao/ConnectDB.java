package kqlhotel.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectDB {
    private static ConnectDB instance = new ConnectDB();
    private Connection connection;

    private ConnectDB() {}

    public static ConnectDB getInstance() {
        return instance;
    }

    public void connect() throws SQLException, ClassNotFoundException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
                // Note: DB name 'QLKhachSan' matches the provided SQL schema
                String url = "jdbc:sqlserver://localhost:1433;databaseName=QLKhachSan;encrypt=false;trustServerCertificate=true";
                String user = "sa";
                String password = "123456";
                connection = DriverManager.getConnection(url, user, password);
            } catch (SQLException e) {
                System.err.println("=== LOI KET NOI DATABASE ===");
                System.err.println("1. Hay dam bao SQL Server dang chay.");
                System.err.println("2. Kiem tra Port 1433 da duoc bat trong SQL Configuration Manager.");
                System.err.println("3. Kiem tra tai khoan 'sa' va mat khau '123456'.");
                System.err.println("4. Dam bao da chay file SQL de tao database 'QLKhachSan'.");
                System.err.println("Chi tiet loi: " + e.getMessage());
                throw e;
            }
        }
    }

    public void disconnect() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // Static method for everyone to use
    public static Connection getConnection() {
        try {
            if (instance.connection == null || instance.connection.isClosed()) {
                instance.connect();
            }
        } catch (Exception e) {
            System.err.println("ConnectDB.getConnection() – tự kết nối lại thất bại: " + e.getMessage());
        }
        return instance.connection;
    }

    // Alias for more clarity
    public static Connection getSqlConnection() throws SQLException {
        try {
            instance.connect();
        } catch (ClassNotFoundException e) {
            throw new SQLException("SQL Driver not found", e);
        }
        return instance.connection;
    }
}
