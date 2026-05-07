package kqlhotel.dao.shift;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import kqlhotel.dao.ConnectDB;

public class ShiftDAO {

    public static class ShiftInfo {
        public final String maPC;
        public final String loaiCa;
        public final String gioBatDau;
        public final String gioKetThuc;
        public final String hoTenNV;
        public final double tienMoCa;
        public final double doanhThu;
        public final int soGiaoDich;

        public ShiftInfo(String maPC, String loaiCa, String gioBatDau, String gioKetThuc,
                         String hoTenNV, double tienMoCa, double doanhThu, int soGiaoDich) {
            this.maPC = maPC;
            this.loaiCa = loaiCa;
            this.gioBatDau = gioBatDau;
            this.gioKetThuc = gioKetThuc;
            this.hoTenNV = hoTenNV;
            this.tienMoCa = tienMoCa;
            this.doanhThu = doanhThu;
            this.soGiaoDich = soGiaoDich;
        }
    }

    private static final String SQL_CURRENT_SHIFT =
        "SELECT TOP 1 pc.maPC, cl.loaiCa, " +
        "  CONVERT(VARCHAR(5), cl.gioBatDau, 108) AS gioBatDau, " +
        "  CONVERT(VARCHAR(5), cl.gioKetThuc, 108) AS gioKetThuc, " +
        "  nv.hoTenNV, pc.tienMoCa, " +
                "  COALESCE(SUM(CASE WHEN tt.trangThaiTT = 'ThanhToanThanhCong' " +
                "                    AND tt.phuongThucTT = 'TienMat' " +
                "                   THEN tt.soTienTT ELSE 0 END), 0) AS doanhThu, " +
                "  COUNT(CASE WHEN tt.trangThaiTT = 'ThanhToanThanhCong' THEN 1 END) AS soGiaoDich " +
        "FROM PhanCongCa pc " +
        "JOIN CaLam cl ON pc.maCa = cl.maCa " +
        "JOIN NhanVien nv ON pc.maNV = nv.maNV " +
        "LEFT JOIN ThanhToan tt ON tt.maPC = pc.maPC " +
        "WHERE pc.tienKetCa = 0 " +
        "GROUP BY pc.maPC, pc.ngay, cl.loaiCa, cl.gioBatDau, cl.gioKetThuc, nv.hoTenNV, pc.tienMoCa " +
        "ORDER BY pc.ngay DESC";

    private static final String SQL_FIND_CALAM_BY_TIME =
        "SELECT TOP 1 maCa FROM CaLam " +
        "WHERE (gioBatDau < gioKetThuc AND CAST(GETDATE() AS TIME) BETWEEN gioBatDau AND gioKetThuc) " +
        "   OR (gioBatDau >= gioKetThuc AND (CAST(GETDATE() AS TIME) >= gioBatDau OR CAST(GETDATE() AS TIME) < gioKetThuc))";

    public boolean openShift(String maNV, long tienMoCa) {
        Connection con = ConnectDB.getConnection();
        if (con == null) return false;

        try {
            String activeMaNV = getLatestOpenShiftStaffId();

            // Nếu đang có nhân viên khác mở ca thì không cho mở thêm ca mới
            if (activeMaNV != null && !activeMaNV.isBlank() && !activeMaNV.equals(maNV)) {
                return false;
            }

            String maCa = findCurrentMaCa(con);
            if (maCa == null) return false;

            // Nếu chính nhân viên này đã có ca đang mở thì coi như thành công,
            // không tạo thêm dòng PhanCongCa mới.
            if (hasOpenShift(con, maNV, maCa)) return true;

            String maPC = nextMaPC(con);
            String sql = "INSERT INTO PhanCongCa (maPC, ngay, tienMoCa, tienKetCa, maNV, maCa) VALUES (?, ?, ?, 0, ?, ?)";

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, maPC);
                ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
                ps.setBigDecimal(3, java.math.BigDecimal.valueOf(tienMoCa));
                ps.setString(4, maNV);
                ps.setString(5, maCa);
                ps.executeUpdate();
            }

            return true;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    private boolean hasOpenShift(Connection con, String maNV, String maCa) throws SQLException {
        String sql = "SELECT COUNT(*) FROM PhanCongCa WHERE maNV = ? AND maCa = ? AND tienKetCa = 0";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maNV);
            ps.setString(2, maCa);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    private String findCurrentMaCa(Connection con) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(SQL_FIND_CALAM_BY_TIME);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getString(1);
        }
        return null;
    }

    private String nextMaPC(Connection con) throws SQLException {
        String sql = "SELECT TOP 1 maPC FROM PhanCongCa WHERE maPC LIKE 'PC%' ORDER BY maPC DESC";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                String last = rs.getString(1);
                int seq = Integer.parseInt(last.substring(2)) + 1;
                return String.format("PC%03d", seq);
            }
        }
        return "PC001";
    }

    public ShiftInfo getOpenShiftByStaff(String maNV) {
        if (maNV == null || maNV.isBlank()) {
            return null;
        }

        Connection con = ConnectDB.getConnection();
        if (con == null) {
            return null;
        }

        String sql =
                "SELECT TOP 1 pc.maPC, cl.loaiCa, " +
                        "  CONVERT(VARCHAR(5), cl.gioBatDau, 108) AS gioBatDau, " +
                        "  CONVERT(VARCHAR(5), cl.gioKetThuc, 108) AS gioKetThuc, " +
                        "  nv.hoTenNV, pc.tienMoCa, " +
                        "  COALESCE(SUM(CASE WHEN tt.trangThaiTT = 'ThanhToanThanhCong' " +
                        "                    AND tt.phuongThucTT = 'TienMat' " +
                        "                   THEN tt.soTienTT ELSE 0 END), 0) AS doanhThu, " +
                        "  COUNT(CASE WHEN tt.trangThaiTT = 'ThanhToanThanhCong' THEN 1 END) AS soGiaoDich " +
                        "FROM PhanCongCa pc " +
                        "JOIN CaLam cl ON pc.maCa = cl.maCa " +
                        "JOIN NhanVien nv ON pc.maNV = nv.maNV " +
                        "LEFT JOIN ThanhToan tt ON tt.maPC = pc.maPC " +
                        "WHERE pc.tienKetCa = 0 AND pc.maNV = ? " +
                        "GROUP BY pc.maPC, pc.ngay, cl.loaiCa, cl.gioBatDau, cl.gioKetThuc, nv.hoTenNV, pc.tienMoCa " +
                        "ORDER BY pc.ngay DESC";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maNV);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new ShiftInfo(
                            rs.getString("maPC"),
                            rs.getString("loaiCa"),
                            rs.getString("gioBatDau"),
                            rs.getString("gioKetThuc"),
                            rs.getString("hoTenNV"),
                            rs.getDouble("tienMoCa"),
                            rs.getDouble("doanhThu"),
                            rs.getInt("soGiaoDich")
                    );
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public ShiftInfo getCurrentShift() {
        Connection con = ConnectDB.getConnection();
        if (con == null) return null;
        try (PreparedStatement ps = con.prepareStatement(SQL_CURRENT_SHIFT);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return new ShiftInfo(
                    rs.getString("maPC"),
                    rs.getString("loaiCa"),
                    rs.getString("gioBatDau"),
                    rs.getString("gioKetThuc"),
                    rs.getString("hoTenNV"),
                    rs.getDouble("tienMoCa"),
                    rs.getDouble("doanhThu"),
                    rs.getInt("soGiaoDich")
                );
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public boolean hasOpenShiftNow(String maNV) {
        if (maNV == null || maNV.isBlank()) {
            return false;
        }

        Connection con = ConnectDB.getConnection();
        if (con == null) {
            return false;
        }

        try {
            String maCa = findCurrentMaCa(con);
            if (maCa == null) {
                return false;
            }

            return hasOpenShift(con, maNV, maCa);
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public String getLatestOpenShiftStaffId() {
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            return null;
        }

        String sql = """
        SELECT TOP 1 maNV
        FROM PhanCongCa
        WHERE tienKetCa = 0
        ORDER BY ngay DESC
    """;

        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getString("maNV");
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public String getOpenShiftIdByStaff(String maNV) {
        if (maNV == null || maNV.isBlank()) {
            return null;
        }

        Connection con = ConnectDB.getConnection();
        if (con == null) {
            return null;
        }

        String sql = """
        SELECT TOP 1 maPC
        FROM PhanCongCa
        WHERE maNV = ?
          AND tienKetCa = 0
        ORDER BY ngay DESC
    """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maNV);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("maPC");
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public boolean closeShift(String maPC, long tienKetCa) {
        if (maPC == null || maPC.isBlank()) {
            return false;
        }

        Connection con = ConnectDB.getConnection();
        if (con == null) {
            return false;
        }

        String sql = """
        UPDATE PhanCongCa
        SET tienKetCa = ?
        WHERE maPC = ?
          AND tienKetCa = 0
    """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setBigDecimal(1, java.math.BigDecimal.valueOf(tienKetCa));
            ps.setString(2, maPC);

            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }
}
