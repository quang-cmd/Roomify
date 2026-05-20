package kqlhotel.dao.payment;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import kqlhotel.dao.ConnectDB;
import kqlhotel.entity.Payment;

public class PaymentDAO {

    public boolean create(Payment payment) {
        try {
            Connection con = ConnectDB.getInstance().getConnection();
            return create(con, payment);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean create(Connection con, Payment payment) throws SQLException {
        if (con == null || payment == null) {
            return false;
        }

        String sql = "INSERT INTO ThanhToan " +
                "(maTT, ngayTT, soTienTT, ghiChu, phuongThucTT, trangThaiTT, loaiGiaoDich, maHD, maPC, maNV) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, payment.getMaTT());
            ps.setTimestamp(2, Timestamp.valueOf(payment.getNgayTT()));
            ps.setBigDecimal(3, java.math.BigDecimal.valueOf(payment.getSoTienTT()));
            ps.setString(4, payment.getGhiChu());
            ps.setString(5, payment.getPhuongThucTT());
            ps.setString(6, payment.getTrangThaiTT());
            ps.setString(7, payment.getLoaiGiaoDich());
            ps.setString(8, payment.getMaHD());
            ps.setString(9, payment.getMaPC());
            ps.setString(10, payment.getMaNV());
            return ps.executeUpdate() > 0;
        }
    }

    public String getNextId() {
        try {
            return getNextId(ConnectDB.getInstance().getConnection());
        } catch (SQLException e) {
            e.printStackTrace();
            return "TT001";
        }
    }

    public String getNextId(Connection con) throws SQLException {
        String sql = "SELECT MAX(maTT) AS maxMaTT FROM ThanhToan WHERE maTT LIKE 'TT%'";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next() && rs.getString("maxMaTT") != null) {
                String max = rs.getString("maxMaTT");
                int num = Integer.parseInt(max.substring(2)) + 1;
                return String.format("TT%03d", num);
            }
        }
        return "TT001";
    }

    public double getTotalPaidByInvoice(String maHD) {
        if (maHD == null || maHD.isBlank()) {
            return 0;
        }

        try {
            return getTotalPaidByInvoice(ConnectDB.getInstance().getConnection(), maHD);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public double getTotalPaidByInvoice(Connection con, String maHD) throws SQLException {
        if (con == null || maHD == null || maHD.isBlank()) {
            return 0;
        }

        String sql =
                "SELECT COALESCE(SUM(soTienTT), 0) AS totalPaid " +
                "FROM ThanhToan " +
                "WHERE maHD = ? AND trangThaiTT = 'ThanhToanThanhCong'";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maHD);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("totalPaid");
                }
            }
        }
        return 0;
    }

    public List<Payment> getByInvoice(String maHD) {
        List<Payment> payments = new ArrayList<>();
        if (maHD == null || maHD.isBlank()) {
            return payments;
        }

        String sql =
                "SELECT maTT, ngayTT, soTienTT, ghiChu, phuongThucTT, trangThaiTT, loaiGiaoDich, maHD, maPC, maNV " +
                "FROM ThanhToan WHERE maHD = ? ORDER BY ngayTT ASC, maTT ASC";

        try {
            Connection con = ConnectDB.getInstance().getConnection();
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, maHD);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        payments.add(mapResultSetToPayment(rs));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return payments;
    }

    public double getSuccessfulRevenue(LocalDateTime start, LocalDateTime end) {
        String sql =
                "SELECT COALESCE(SUM(CASE " +
                "  WHEN loaiGiaoDich = 'HoanTien' THEN -soTienTT " +
                "  ELSE soTienTT END), 0) AS total " +
                "FROM ThanhToan " +
                "WHERE ngayTT >= ? AND ngayTT < ? " +
                "  AND trangThaiTT = 'ThanhToanThanhCong'";

        try {
            Connection con = ConnectDB.getInstance().getConnection();
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setTimestamp(1, Timestamp.valueOf(start));
                ps.setTimestamp(2, Timestamp.valueOf(end));
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getDouble("total");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public Map<LocalDate, Double> getSuccessfulDailyRevenue(LocalDate start, LocalDate end) {
        Map<LocalDate, Double> revenueByDate = new LinkedHashMap<>();
        String sql =
                "SELECT CAST(ngayTT AS DATE) AS dt, " +
                "COALESCE(SUM(CASE WHEN loaiGiaoDich = 'HoanTien' THEN -soTienTT ELSE soTienTT END), 0) AS total " +
                "FROM ThanhToan " +
                "WHERE ngayTT >= ? AND ngayTT < ? " +
                "  AND trangThaiTT = 'ThanhToanThanhCong' " +
                "GROUP BY CAST(ngayTT AS DATE)";

        try {
            Connection con = ConnectDB.getInstance().getConnection();
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setTimestamp(1, Timestamp.valueOf(start.atStartOfDay()));
                ps.setTimestamp(2, Timestamp.valueOf(end.plusDays(1).atStartOfDay()));
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        revenueByDate.put(rs.getDate("dt").toLocalDate(), rs.getDouble("total"));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return revenueByDate;
    }

    public Map<String, Double> getSuccessfulMonthlyRevenue(LocalDate start, LocalDate end) {
        Map<String, Double> revenueByMonth = new LinkedHashMap<>();
        String sql =
                "SELECT YEAR(ngayTT) AS yr, MONTH(ngayTT) AS mo, " +
                "COALESCE(SUM(CASE WHEN loaiGiaoDich = 'HoanTien' THEN -soTienTT ELSE soTienTT END), 0) AS total " +
                "FROM ThanhToan " +
                "WHERE ngayTT >= ? AND ngayTT < ? " +
                "  AND trangThaiTT = 'ThanhToanThanhCong' " +
                "GROUP BY YEAR(ngayTT), MONTH(ngayTT)";

        try {
            Connection con = ConnectDB.getInstance().getConnection();
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setTimestamp(1, Timestamp.valueOf(start.atStartOfDay()));
                ps.setTimestamp(2, Timestamp.valueOf(end.plusDays(1).atStartOfDay()));
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String key = String.format("%02d/%02d", rs.getInt("mo"), rs.getInt("yr") % 100);
                        revenueByMonth.put(key, rs.getDouble("total"));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return revenueByMonth;
    }

    private Payment mapResultSetToPayment(ResultSet rs) throws SQLException {
        Timestamp ngayTT = rs.getTimestamp("ngayTT");
        return new Payment(
                rs.getString("maTT"),
                ngayTT == null ? null : ngayTT.toLocalDateTime(),
                rs.getDouble("soTienTT"),
                rs.getString("ghiChu"),
                rs.getString("phuongThucTT"),
                rs.getString("trangThaiTT"),
                rs.getString("loaiGiaoDich"),
                rs.getString("maHD"),
                rs.getString("maPC"),
                rs.getString("maNV")
        );
    }
}
