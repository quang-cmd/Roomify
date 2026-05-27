package kqlhotel.dao.shift;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import kqlhotel.dao.ConnectDB;
import kqlhotel.entity.shift.ShiftInfo;
import kqlhotel.entity.shift.ShiftReconciliationRow;

public class ShiftDAO {

    private static final String SQL_CURRENT_SHIFT =
        "SELECT TOP 1 pc.maPC, cl.loaiCa, " +
        "  CONVERT(VARCHAR(5), cl.gioBatDau, 108) AS gioBatDau, " +
        "  CONVERT(VARCHAR(5), cl.gioKetThuc, 108) AS gioKetThuc, " +
        "  nv.hoTenNV, pc.tienMoCa, " +
                "  COALESCE(SUM(CASE " +
                "    WHEN tt.trangThaiTT = 'ThanhToanThanhCong' AND tt.loaiGD = 'HoanTien' THEN -tt.soTienTT " +
                "    WHEN tt.trangThaiTT = 'ThanhToanThanhCong' THEN tt.soTienTT " +
                "    ELSE 0 END), 0) AS doanhThu, " +
                "  COUNT(CASE WHEN tt.trangThaiTT = 'ThanhToanThanhCong' THEN 1 END) AS soGiaoDich " +
        "FROM PhanCongCa pc " +
        "JOIN CaLam cl ON pc.maCa = cl.maCa " +
        "JOIN NhanVien nv ON pc.maNV = nv.maNV " +
        "LEFT JOIN ThanhToan tt ON tt.maPC = pc.maPC " +
        "WHERE pc.trangThai = N'DangMo' " +
        "  AND CAST(pc.ngay AS DATE) = CAST(GETDATE() AS DATE) " +
        "GROUP BY pc.maPC, pc.ngay, cl.loaiCa, cl.gioBatDau, cl.gioKetThuc, nv.hoTenNV, pc.tienMoCa " +
        "ORDER BY pc.ngay DESC";

    private static final String SQL_FIND_CALAM_BY_TIME =
        "SELECT TOP 1 maCa FROM CaLam " +
        "WHERE (gioBatDau < gioKetThuc AND CAST(GETDATE() AS TIME) >= gioBatDau AND CAST(GETDATE() AS TIME) < gioKetThuc) " +
        "   OR (gioBatDau >= gioKetThuc AND (CAST(GETDATE() AS TIME) >= gioBatDau OR CAST(GETDATE() AS TIME) < gioKetThuc))";

    public boolean openShift(String maNV, long tienMoCa) {
        Connection con = ConnectDB.getConnection();
        if (con == null) return false;

        try {
            String maCa = findCurrentMaCa(con);
            if (maCa == null) return false;

            String activeMaNV = getLatestOpenShiftStaffId();

            // Nếu đang có nhân viên khác mở ca thì không cho mở thêm ca mới
            if (activeMaNV != null && !activeMaNV.isBlank() && !activeMaNV.equals(maNV)) {
                return false;
            }
            if (activeMaNV != null && activeMaNV.equals(maNV)) {
                return true;
            }

            if (hasOpenShift(con, maNV, maCa)) return true;

            AssignedShift assignedShift = getAssignedShiftForCurrentShift(con, maCa);
            if (assignedShift != null) {
                if (!assignedShift.maNV.equals(maNV)) {
                    return false;
                }

                return activateAssignedShift(con, assignedShift.maPC, tienMoCa);
            }

            return false;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean canOpenShift(String maNV) {
        if (maNV == null || maNV.isBlank()) {
            return false;
        }

        String activeMaNV = getLatestOpenShiftStaffId();
        if (activeMaNV != null && !activeMaNV.isBlank()) {
            return activeMaNV.equals(maNV);
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

            AssignedShift assignedShift = getAssignedShiftForCurrentShift(con, maCa);
            if (assignedShift != null) {
                return assignedShift.maNV.equals(maNV);
            }

            return false;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    private boolean activateAssignedShift(Connection con, String maPC, long tienMoCa) throws SQLException {
        String sql = """
            UPDATE PhanCongCa
            SET tienMoCa = ?,
                thoiGianMoCa = GETDATE(),
                thoiGianKetCa = NULL,
                trangThai = N'DangMo'
            WHERE maPC = ?
              AND trangThai = N'DaPhanCong'
              AND CAST(ngay AS DATE) = CAST(GETDATE() AS DATE)
        """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setBigDecimal(1, java.math.BigDecimal.valueOf(tienMoCa));
            ps.setString(2, maPC);
            return ps.executeUpdate() > 0;
        }
    }

    private AssignedShift getAssignedShiftForCurrentShift(Connection con, String maCa) throws SQLException {
        String sql = """
            SELECT TOP 1 pc.maPC, pc.maNV, pc.maCa
            FROM PhanCongCa pc
            WHERE pc.trangThai = N'DaPhanCong'
              AND pc.maCa = ?
              AND CAST(pc.ngay AS DATE) = CAST(GETDATE() AS DATE)
            ORDER BY pc.maPC ASC
        """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maCa);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new AssignedShift(
                        rs.getString("maPC"),
                        rs.getString("maNV"),
                        rs.getString("maCa")
                    );
                }
            }
        }

        return null;
    }

    private boolean hasOpenShift(Connection con, String maNV, String maCa) throws SQLException {
        String sql = """
            SELECT COUNT(*)
            FROM PhanCongCa
            WHERE maNV = ?
              AND maCa = ?
              AND trangThai = N'DangMo'
              AND CAST(ngay AS DATE) = CAST(GETDATE() AS DATE)
        """;
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

    private static class AssignedShift {
        private final String maPC;
        private final String maNV;
        private final String maCa;

        private AssignedShift(String maPC, String maNV, String maCa) {
            this.maPC = maPC;
            this.maNV = maNV;
            this.maCa = maCa;
        }
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
                        "  COALESCE(SUM(CASE " +
                        "    WHEN tt.trangThaiTT = 'ThanhToanThanhCong' AND tt.loaiGD = 'HoanTien' THEN -tt.soTienTT " +
                        "    WHEN tt.trangThaiTT = 'ThanhToanThanhCong' THEN tt.soTienTT " +
                        "    ELSE 0 END), 0) AS doanhThu, " +
                        "  COUNT(CASE WHEN tt.trangThaiTT = 'ThanhToanThanhCong' THEN 1 END) AS soGiaoDich " +
                        "FROM PhanCongCa pc " +
                        "JOIN CaLam cl ON pc.maCa = cl.maCa " +
                        "JOIN NhanVien nv ON pc.maNV = nv.maNV " +
                        "LEFT JOIN ThanhToan tt ON tt.maPC = pc.maPC " +
                        "WHERE pc.trangThai = N'DangMo' AND pc.maNV = ? " +
                        "  AND CAST(pc.ngay AS DATE) = CAST(GETDATE() AS DATE) " +
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

    public List<ShiftReconciliationRow> getRecentShiftReconciliations(int limit) {
        List<ShiftReconciliationRow> rows = new ArrayList<>();
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            return rows;
        }

        String sql = """
            SELECT TOP (?) pc.maPC, nv.hoTenNV, cl.loaiCa,
                   DATEADD(SECOND, DATEDIFF(SECOND, CAST('00:00:00' AS TIME), cl.gioBatDau), CAST(CAST(pc.ngay AS DATE) AS DATETIME2)) AS thoiGianDuKienMoCa,
                   pc.thoiGianMoCa, pc.thoiGianKetCa,
                   pc.tienMoCa, pc.tienKetCa, pc.trangThai,
                   COALESCE(SUM(CASE
                       WHEN pc.trangThai = N'DaPhanCong'
                       THEN 0
                       WHEN tt.trangThaiTT = 'ThanhToanThanhCong' AND tt.loaiGD = 'HoanTien'
                       THEN -tt.soTienTT
                       WHEN tt.trangThaiTT = 'ThanhToanThanhCong'
                       THEN tt.soTienTT
                       ELSE 0
                   END), 0) AS doanhThuHeThong,
                   COALESCE(SUM(CASE
                       WHEN pc.trangThai = N'DaPhanCong'
                       THEN 0
                       WHEN tt.trangThaiTT = 'ThanhToanThanhCong'
                            AND tt.phuongThucTT = 'TienMat'
                            AND tt.loaiGD = 'HoanTien'
                       THEN -tt.soTienTT
                       WHEN tt.trangThaiTT = 'ThanhToanThanhCong'
                            AND tt.phuongThucTT = 'TienMat'
                       THEN tt.soTienTT
                       ELSE 0
                   END), 0) AS doanhThuTienMat
            FROM PhanCongCa pc
            JOIN CaLam cl ON pc.maCa = cl.maCa
            JOIN NhanVien nv ON pc.maNV = nv.maNV
            LEFT JOIN ThanhToan tt ON tt.maPC = pc.maPC
            GROUP BY pc.maPC, pc.ngay, nv.hoTenNV, cl.loaiCa, cl.gioBatDau,
                     pc.thoiGianMoCa, pc.thoiGianKetCa,
                     pc.tienMoCa, pc.tienKetCa, pc.trangThai
            ORDER BY
                CASE WHEN pc.thoiGianMoCa IS NULL THEN 1 ELSE 0 END,
                pc.thoiGianMoCa DESC,
                pc.ngay DESC,
                pc.maPC DESC
        """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, Math.max(1, limit));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Timestamp openedAt = rs.getTimestamp("thoiGianMoCa");
                    Timestamp closedAt = rs.getTimestamp("thoiGianKetCa");

                    rows.add(new ShiftReconciliationRow(
                        rs.getString("maPC"),
                        rs.getString("hoTenNV"),
                        rs.getString("loaiCa"),
                        rs.getTimestamp("thoiGianDuKienMoCa") == null ? null : rs.getTimestamp("thoiGianDuKienMoCa").toLocalDateTime(),
                        openedAt == null ? null : openedAt.toLocalDateTime(),
                        closedAt == null ? null : closedAt.toLocalDateTime(),
                        rs.getDouble("tienMoCa"),
                        rs.getDouble("tienKetCa"),
                        rs.getDouble("doanhThuHeThong"),
                        rs.getDouble("doanhThuTienMat"),
                        rs.getString("trangThai")
                    ));
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return rows;
    }

    public List<ShiftReconciliationRow> getActiveAndAssignedShiftReconciliations(int limit) {
        List<ShiftReconciliationRow> rows = new ArrayList<>();
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            return rows;
        }

        String sql = """
            SELECT TOP (?) pc.maPC, nv.hoTenNV, cl.loaiCa,
                   DATEADD(SECOND, DATEDIFF(SECOND, CAST('00:00:00' AS TIME), cl.gioBatDau), CAST(CAST(pc.ngay AS DATE) AS DATETIME2)) AS thoiGianDuKienMoCa,
                   pc.thoiGianMoCa, pc.thoiGianKetCa,
                   pc.tienMoCa, pc.tienKetCa, pc.trangThai,
                   COALESCE(SUM(CASE
                       WHEN pc.trangThai = N'DaPhanCong'
                       THEN 0
                       WHEN tt.trangThaiTT = 'ThanhToanThanhCong' AND tt.loaiGD = 'HoanTien'
                       THEN -tt.soTienTT
                       WHEN tt.trangThaiTT = 'ThanhToanThanhCong'
                       THEN tt.soTienTT
                       ELSE 0
                   END), 0) AS doanhThuHeThong,
                   COALESCE(SUM(CASE
                       WHEN pc.trangThai = N'DaPhanCong'
                       THEN 0
                       WHEN tt.trangThaiTT = 'ThanhToanThanhCong'
                            AND tt.phuongThucTT = 'TienMat'
                            AND tt.loaiGD = 'HoanTien'
                       THEN -tt.soTienTT
                       WHEN tt.trangThaiTT = 'ThanhToanThanhCong'
                            AND tt.phuongThucTT = 'TienMat'
                       THEN tt.soTienTT
                       ELSE 0
                   END), 0) AS doanhThuTienMat
            FROM PhanCongCa pc
            JOIN CaLam cl ON pc.maCa = cl.maCa
            JOIN NhanVien nv ON pc.maNV = nv.maNV
            LEFT JOIN ThanhToan tt ON tt.maPC = pc.maPC
            WHERE (
                    pc.trangThai = N'DangMo'
                    AND CAST(pc.ngay AS DATE) = CAST(GETDATE() AS DATE)
                  )
               OR (
                    pc.trangThai = N'DaPhanCong'
                    AND CAST(pc.ngay AS DATE) >= CAST(GETDATE() AS DATE)
                  )
            GROUP BY pc.maPC, pc.ngay, nv.hoTenNV, cl.loaiCa, cl.gioBatDau,
                     pc.thoiGianMoCa, pc.thoiGianKetCa,
                     pc.tienMoCa, pc.tienKetCa, pc.trangThai
            ORDER BY
                CASE
                    WHEN pc.trangThai = N'DangMo' THEN 0
                    WHEN pc.trangThai = N'DaPhanCong' THEN 1
                    ELSE 2
                END,
                pc.ngay ASC,
                CASE cl.loaiCa
                    WHEN 'CaSang' THEN 1
                    WHEN 'CaChieu' THEN 2
                    WHEN 'CaToi' THEN 3
                    ELSE 4
                END,
                pc.maPC ASC
        """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, Math.max(1, limit));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Timestamp openedAt = rs.getTimestamp("thoiGianMoCa");
                    Timestamp closedAt = rs.getTimestamp("thoiGianKetCa");

                    rows.add(new ShiftReconciliationRow(
                        rs.getString("maPC"),
                        rs.getString("hoTenNV"),
                        rs.getString("loaiCa"),
                        rs.getTimestamp("thoiGianDuKienMoCa") == null ? null : rs.getTimestamp("thoiGianDuKienMoCa").toLocalDateTime(),
                        openedAt == null ? null : openedAt.toLocalDateTime(),
                        closedAt == null ? null : closedAt.toLocalDateTime(),
                        rs.getDouble("tienMoCa"),
                        rs.getDouble("tienKetCa"),
                        rs.getDouble("doanhThuHeThong"),
                        rs.getDouble("doanhThuTienMat"),
                        rs.getString("trangThai")
                    ));
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return rows;
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
        WHERE trangThai = N'DangMo'
          AND CAST(ngay AS DATE) = CAST(GETDATE() AS DATE)
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
          AND trangThai = N'DangMo'
          AND CAST(ngay AS DATE) = CAST(GETDATE() AS DATE)
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
            SET tienKetCa = ?,
                thoiGianKetCa = GETDATE(),
                trangThai = N'DaKet'
            WHERE maPC = ?
              AND trangThai = N'DangMo'
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
