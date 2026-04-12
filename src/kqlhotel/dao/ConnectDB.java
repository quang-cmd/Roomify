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
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            String url = "jdbc:sqlserver://localhost:1433;databaseName=QuanLyKhachSan;encrypt=false";
            String user = "sa";
            String password = "123";
            connection = DriverManager.getConnection(url, user, password);
        }
    }

    public void disconnect() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public Connection getConnection() {
        return connection;
    }
}
