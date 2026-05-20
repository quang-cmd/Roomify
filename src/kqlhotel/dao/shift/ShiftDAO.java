package kqlhotel.dao.shift;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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

    public static class ShiftReconciliationRow {
        public final String maPC;
        public final String hoTenNV;
        public final String loaiCa;
        public final LocalDateTime thoiGianMoCa;
        public final LocalDateTime thoiGianKetCa;
        public final double tienMoCa;
        public final double tienKetCa;
        public final double doanhThuHeThong;
        public final double doanhThuTienMat;
        public final String trangThai;

        public ShiftReconciliationRow(String maPC, String hoTenNV, String loaiCa,
                                      LocalDateTime thoiGianMoCa, LocalDateTime thoiGianKetCa,
                                      double tienMoCa, double tienKetCa,
                                      double doanhThuHeThong, double doanhThuTienMat, String trangThai) {
            this.maPC = maPC;
            this.hoTenNV = hoTenNV;
            this.loaiCa = loaiCa;
            this.thoiGianMoCa = thoiGianMoCa;
            this.thoiGianKetCa = thoiGianKetCa;
            this.tienMoCa = tienMoCa;
            this.tienKetCa = tienKetCa;
            this.doanhThuHeThong = doanhThuHeThong;
            this.doanhThuTienMat = doanhThuTienMat;
            this.trangThai = trangThai;
        }
    }

    private static final String SQL_CURRENT_SHIFT =
        "SELECT TOP 1 pc.maPC, cl.loaiCa, " +
        "  CONVERT(VARCHAR(5), cl.gioBatDau, 108) AS gioBatDau, " +
        "  CONVERT(VARCHAR(5), cl.gioKetThuc, 108) AS gioKetThuc, " +
        "  nv.hoTenNV, pc.tienMoCa, " +
                "  COALESCE(SUM(CASE WHEN tt.trangThaiTT = 'ThanhToanThanhCong' " +
                "                   THEN tt.soTienTT ELSE 0 END), 0) AS doanhThu, " +
                "  COUNT(CASE WHEN tt.trangThaiTT = 'ThanhToanThanhCong' THEN 1 END) AS soGiaoDich " +
        "FROM PhanCongCa pc " +
        "JOIN CaLam cl ON pc.maCa = cl.maCa " +
        "JOIN NhanVien nv ON pc.maNV = nv.maNV " +
        "LEFT JOIN ThanhToan tt ON tt.maPC = pc.maPC " +
        "WHERE pc.trangThai = N'DangMo' " +
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
            String assignedMaNV = getAssignedStaffForCurrentShift(con, maCa);
            if (assignedMaNV != null && !assignedMaNV.isBlank() && !assignedMaNV.equals(maNV)) {
                return false;
            }

            if (hasOpenShift(con, maNV, maCa)) return true;

            // Uu tien kich hoat ca da duoc phan cong san trong ngay.
            // Neu khong co lich phan cong phu hop moi tao ca phat sinh.
            if (activateAssignedShift(con, maNV, maCa, tienMoCa)) {
                return true;
            }

            String maPC = nextMaPC(con);
            String sql = """
                INSERT INTO PhanCongCa (
                    maPC, ngay, tienMoCa, tienKetCa,
                    thoiGianMoCa, thoiGianKetCa, trangThai,
                    maNV, maCa
                )
                VALUES (?, ?, ?, 0, GETDATE(), NULL, N'DangMo', ?, ?)
            """;

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

    private boolean activateAssignedShift(Connection con, String maNV, String maCa, long tienMoCa) throws SQLException {
        String sql = """
            UPDATE PhanCongCa
            SET tienMoCa = ?,
                thoiGianMoCa = GETDATE(),
                thoiGianKetCa = NULL,
                trangThai = N'DangMo'
            WHERE maNV = ?
              AND maCa = ?
              AND trangThai = N'DaPhanCong'
              AND CAST(ngay AS DATE) = CAST(GETDATE() AS DATE)
        """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setBigDecimal(1, java.math.BigDecimal.valueOf(tienMoCa));
            ps.setString(2, maNV);
            ps.setString(3, maCa);
            return ps.executeUpdate() > 0;
        }
    }

    private String getAssignedStaffForCurrentShift(Connection con, String maCa) throws SQLException {
        String sql = """
            SELECT TOP 1 maNV
            FROM PhanCongCa
            WHERE maCa = ?
              AND trangThai = N'DaPhanCong'
              AND CAST(ngay AS DATE) = CAST(GETDATE() AS DATE)
            ORDER BY maPC ASC
        """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maCa);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("maNV");
                }
            }
        }

        return null;
    }

    private boolean hasOpenShift(Connection con, String maNV, String maCa) throws SQLException {
        String sql = "SELECT COUNT(*) FROM PhanCongCa WHERE maNV = ? AND maCa = ? AND trangThai = N'DangMo'";
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
                        "                   THEN tt.soTienTT ELSE 0 END), 0) AS doanhThu, " +
                        "  COUNT(CASE WHEN tt.trangThaiTT = 'ThanhToanThanhCong' THEN 1 END) AS soGiaoDich " +
                        "FROM PhanCongCa pc " +
                        "JOIN CaLam cl ON pc.maCa = cl.maCa " +
                        "JOIN NhanVien nv ON pc.maNV = nv.maNV " +
                        "LEFT JOIN ThanhToan tt ON tt.maPC = pc.maPC " +
                        "WHERE pc.trangThai = N'DangMo' AND pc.maNV = ? " +
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
                   pc.thoiGianMoCa, pc.thoiGianKetCa,
                   pc.tienMoCa, pc.tienKetCa, pc.trangThai,
                   COALESCE(SUM(CASE
                       WHEN tt.trangThaiTT = 'ThanhToanThanhCong'
                       THEN tt.soTienTT
                       ELSE 0
                   END), 0) AS doanhThuHeThong,
                   COALESCE(SUM(CASE
                       WHEN tt.trangThaiTT = 'ThanhToanThanhCong'
                            AND tt.phuongThucTT = 'TienMat'
                       THEN tt.soTienTT
                       ELSE 0
                   END), 0) AS doanhThuTienMat
            FROM PhanCongCa pc
            JOIN CaLam cl ON pc.maCa = cl.maCa
            JOIN NhanVien nv ON pc.maNV = nv.maNV
            LEFT JOIN ThanhToan tt ON tt.maPC = pc.maPC
            GROUP BY pc.maPC, pc.ngay, nv.hoTenNV, cl.loaiCa,
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
                   pc.thoiGianMoCa, pc.thoiGianKetCa,
                   pc.tienMoCa, pc.tienKetCa, pc.trangThai,
                   COALESCE(SUM(CASE
                       WHEN tt.trangThaiTT = 'ThanhToanThanhCong'
                       THEN tt.soTienTT
                       ELSE 0
                   END), 0) AS doanhThuHeThong,
                   COALESCE(SUM(CASE
                       WHEN tt.trangThaiTT = 'ThanhToanThanhCong'
                            AND tt.phuongThucTT = 'TienMat'
                       THEN tt.soTienTT
                       ELSE 0
                   END), 0) AS doanhThuTienMat
            FROM PhanCongCa pc
            JOIN CaLam cl ON pc.maCa = cl.maCa
            JOIN NhanVien nv ON pc.maNV = nv.maNV
            LEFT JOIN ThanhToan tt ON tt.maPC = pc.maPC
            WHERE pc.trangThai IN (N'DangMo', N'DaPhanCong')
            GROUP BY pc.maPC, pc.ngay, nv.hoTenNV, cl.loaiCa,
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
