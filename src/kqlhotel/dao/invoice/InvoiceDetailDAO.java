package kqlhotel.dao.invoice;

import kqlhotel.dao.ConnectDB;
import kqlhotel.entity.InvoiceDetail;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class InvoiceDetailDAO {

    public List<InvoiceDetail> getByInvoice(String maHD) {
        List<InvoiceDetail> list = new ArrayList<>();

        try {
            Connection con = ConnectDB.getConnection();
            String sql = "SELECT maHD, maPhong, ngayNhanPhong, ngayTraPhong, ngayTraThucTe, soDem, phuThu, thanhTien " +
                    "FROM ChiTietHoaDon WHERE maHD = ? ORDER BY maPhong";
            PreparedStatement pstmt = con.prepareStatement(sql);
            pstmt.setString(1, maHD);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                InvoiceDetail ct = new InvoiceDetail();

                ct.setMaHD(rs.getString("maHD"));
                ct.setMaPhong(rs.getString("maPhong"));

                Timestamp ngayNhanPhong = rs.getTimestamp("ngayNhanPhong");
                if (ngayNhanPhong != null) {
                    ct.setNgayNhanPhong(ngayNhanPhong.toLocalDateTime());
                }

                Timestamp ngayTraPhong = rs.getTimestamp("ngayTraPhong");
                if (ngayTraPhong != null) {
                    ct.setNgayTraPhong(ngayTraPhong.toLocalDateTime());
                }

                Timestamp ngayTraThucTe = rs.getTimestamp("ngayTraThucTe");
                if (ngayTraThucTe != null) {
                    ct.setNgayTraThucTe(ngayTraThucTe.toLocalDateTime());
                }

                ct.setSoDem(rs.getInt("soDem"));
                ct.setPhuThu(rs.getDouble("phuThu"));
                ct.setThanhTien(rs.getDouble("thanhTien"));

                list.add(ct);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public boolean updateCheckoutInfo(String maHD, String maPhong, LocalDateTime ngayTraThucTe, int soDem, double thanhTien) {
        try {
            Connection con = ConnectDB.getConnection();
            String sql = "UPDATE ChiTietHoaDon " +
                    "SET ngayTraThucTe = ?, soDem = ?, thanhTien = ? " +
                    "WHERE maHD = ? AND maPhong = ?";

            PreparedStatement pstmt = con.prepareStatement(sql);
            pstmt.setTimestamp(1, Timestamp.valueOf(ngayTraThucTe));
            pstmt.setInt(2, soDem);
            pstmt.setDouble(3, thanhTien);
            pstmt.setString(4, maHD);
            pstmt.setString(5, maPhong);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean markAllRemainingRoomsCheckedOut(String maHD, LocalDateTime checkoutTime) {
        try {
            Connection con = ConnectDB.getConnection();
            String sql = "UPDATE ChiTietHoaDon " +
                    "SET ngayTraThucTe = COALESCE(ngayTraThucTe, ?) " +
                    "WHERE maHD = ? AND ngayTraThucTe IS NULL";

            PreparedStatement pstmt = con.prepareStatement(sql);
            pstmt.setTimestamp(1, Timestamp.valueOf(checkoutTime));
            pstmt.setString(2, maHD);

            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public List<InvoiceDetail> getByBooking(String maHD, String maDatPhong) {
        List<InvoiceDetail> list = new ArrayList<>();

        if (maDatPhong == null || maDatPhong.isBlank()) {
            return list;
        }

        try {
            Connection con = ConnectDB.getConnection();
            String sql = """
            SELECT maPhong, ngayNhanDuKien, ngayTraDuKien, donGiaDat
            FROM ChiTietDatPhong
            WHERE maDatPhong = ?
            ORDER BY maPhong
        """;

            PreparedStatement pstmt = con.prepareStatement(sql);
            pstmt.setString(1, maDatPhong);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                InvoiceDetail ct = new InvoiceDetail();

                ct.setMaHD(maHD);
                ct.setMaPhong(rs.getString("maPhong"));

                Timestamp ngayNhan = rs.getTimestamp("ngayNhanDuKien");
                Timestamp ngayTra = rs.getTimestamp("ngayTraDuKien");

                if (ngayNhan != null) {
                    ct.setNgayNhanPhong(ngayNhan.toLocalDateTime());
                }

                if (ngayTra != null) {
                    ct.setNgayTraPhong(ngayTra.toLocalDateTime());
                }

                int soDem = 1;
                if (ct.getNgayNhanPhong() != null && ct.getNgayTraPhong() != null) {
                    soDem = (int) Math.max(
                            1,
                            java.time.temporal.ChronoUnit.DAYS.between(
                                    ct.getNgayNhanPhong().toLocalDate(),
                                    ct.getNgayTraPhong().toLocalDate()
                            )
                    );
                }

                double donGia = rs.getDouble("donGiaDat");

                ct.setNgayTraThucTe(null);
                ct.setSoDem(soDem);
                ct.setPhuThu(0);
                ct.setThanhTien(donGia * soDem);

                list.add(ct);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }
    public boolean createFromBookingIfMissing(String maHD, String maDatPhong, List<String> roomCodes) {
        if (maHD == null || maDatPhong == null || roomCodes == null || roomCodes.isEmpty()) {
            return false;
        }

        try {
            Connection con = ConnectDB.getConnection();

            String sql =
                    "INSERT INTO ChiTietHoaDon " +
                            "(maHD, maPhong, ngayNhanPhong, ngayTraPhong, ngayTraThucTe, soDem, phuThu, thanhTien) " +
                            "SELECT ?, ctdp.maPhong, GETDATE(), ctdp.ngayTraDuKien, NULL, " +
                            "CASE WHEN DATEDIFF(DAY, CAST(GETDATE() AS DATE), CAST(ctdp.ngayTraDuKien AS DATE)) <= 0 " +
                            "THEN 1 ELSE DATEDIFF(DAY, CAST(GETDATE() AS DATE), CAST(ctdp.ngayTraDuKien AS DATE)) END, " +
                            "0, " +
                            "ctdp.donGiaDat * " +
                            "CASE WHEN DATEDIFF(DAY, CAST(GETDATE() AS DATE), CAST(ctdp.ngayTraDuKien AS DATE)) <= 0 " +
                            "THEN 1 ELSE DATEDIFF(DAY, CAST(GETDATE() AS DATE), CAST(ctdp.ngayTraDuKien AS DATE)) END " +
                            "FROM ChiTietDatPhong ctdp " +
                            "WHERE ctdp.maDatPhong = ? " +
                            "AND ctdp.maPhong = ? " +
                            "AND NOT EXISTS (SELECT 1 FROM ChiTietHoaDon WHERE maHD = ? AND maPhong = ?)";

            boolean ok = true;

            for (String roomCode : roomCodes) {
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setString(1, maHD);
                    ps.setString(2, maDatPhong);
                    ps.setString(3, roomCode);
                    ps.setString(4, maHD);
                    ps.setString(5, roomCode);
                    ps.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                    ok = false;
                }
            }

            return ok;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}