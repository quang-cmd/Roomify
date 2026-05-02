package kqlhotel.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectDB {
    private static ConnectDB instance = new ConnectDB();
    private Connection connection;

    private ConnectDB() {
    }

    public static ConnectDB getInstance() {
        return instance;
    }

    public void connect() throws SQLException, ClassNotFoundException {
        if (connection == null || connection.isClosed()) {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            // Note: DB name 'QLKhachSan' matches the provided SQL schema
            String url = "jdbc:sqlserver://localhost:1433;databaseName=QLKhachSan;encrypt=false;trustServerCertificate=true";
            String user = "sa";
            String password = "123456";
            connection = DriverManager.getConnection(url, user, password);
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

    public static Connection getConnection() {
        try {
            if (instance.connection == null || instance.connection.isClosed()) {
                instance.connect();
            }
        } catch (Exception e) {
            System.err.println("ConnectDB.getInstance().getConnection() – tự kết nối lại thất bại: " + e.getMessage());
        }
        return instance.connection;
    }

    // Static helper for convenience
    public static Connection getSqlConnection() throws SQLException {
        try {
            instance.connect();
        } catch (ClassNotFoundException e) {
            throw new SQLException("SQL Server driver not found", e);
        }
        return instance.connection;
    }
}