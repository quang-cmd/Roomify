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
            Connection con = ConnectDB.getInstance().getConnection();
            String sql = "SELECT maHD, maPhong, ngayNhanPhong, ngayTraPhong, ngayTraThucTe, soDem, phuThu, phiPhat, thanhTien " +
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
                ct.setPhiPhat(rs.getDouble("phiPhat"));
                ct.setThanhTien(rs.getDouble("thanhTien"));

                list.add(ct);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public boolean updateCheckoutInfo(String maHD, String maPhong, LocalDateTime ngayTraThucTe, int soDem, double phuThu,double phiPhat, double thanhTien) {
        try {
            Connection con = ConnectDB.getInstance().getConnection();
            String sql = "UPDATE ChiTietHoaDon " +
                    "SET ngayTraThucTe = ?, soDem = ?, phuThu = ?, phiPhat = ?, thanhTien = ? " +
                    "WHERE maHD = ? AND maPhong = ?";

            PreparedStatement pstmt = con.prepareStatement(sql);
            pstmt.setTimestamp(1, Timestamp.valueOf(ngayTraThucTe));
            pstmt.setInt(2, soDem);
            pstmt.setDouble(3, phuThu);
            pstmt.setDouble(4, phiPhat); // Default phiPhat to 0 during normal checkout update
            pstmt.setDouble(5, thanhTien);
            pstmt.setString(6, maHD);
            pstmt.setString(7, maPhong);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean markAllRemainingRoomsCheckedOut(String maHD, LocalDateTime checkoutTime) {
        try {
            Connection con = ConnectDB.getInstance().getConnection();
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

        try {
            Connection con = ConnectDB.getInstance().getConnection();
            String sql = """
        SELECT maPhong, ngayNhanDuKien, ngayTraDuKien, donGiaDat
        FROM ChiTietDatPhong
        WHERE maDatPhong = ?
        """;

            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, maDatPhong);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                InvoiceDetail ct = new InvoiceDetail();

                ct.setMaHD(maHD);
                ct.setMaPhong(rs.getString("maPhong"));

                LocalDateTime expectedIn = rs.getTimestamp("ngayNhanDuKien").toLocalDateTime();
                LocalDateTime expectedOut = rs.getTimestamp("ngayTraDuKien").toLocalDateTime();

                ct.setNgayNhanPhong(expectedIn); // 👉 dùng dự kiến
                ct.setNgayTraPhong(expectedOut);

                long days = java.time.temporal.ChronoUnit.DAYS.between(
                        expectedIn.toLocalDate(),
                        expectedOut.toLocalDate()
                );

                if (days <= 0) days = 1;

                double price = rs.getDouble("donGiaDat");

                ct.setSoDem((int) days);
                ct.setPhuThu(0);
                ct.setThanhTien(days * price);

                list.add(ct);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }
    public boolean createFromBookingIfMissing(String maHD, String maDatPhong, List<String> roomCodes) {
        if (maHD == null || maDatPhong == null || roomCodes == null || roomCodes.isEmpty()) {
            return false;
        }

        try {
            Connection con = ConnectDB.getInstance().getConnection();

            String sql =
                    "INSERT INTO ChiTietHoaDon " +
                            "(maHD, maPhong, ngayNhanPhong, ngayTraPhong, ngayTraThucTe, soDem, phuThu, phiPhat, thanhTien) " +
                            "SELECT ?, ctdp.maPhong, GETDATE(), ctdp.ngayTraDuKien, NULL, " +
                            "CASE WHEN DATEDIFF(DAY, CAST(GETDATE() AS DATE), CAST(ctdp.ngayTraDuKien AS DATE)) <= 0 " +
                            "THEN 1 ELSE DATEDIFF(DAY, CAST(GETDATE() AS DATE), CAST(ctdp.ngayTraDuKien AS DATE)) END, " +
                            "0, 0, " +
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
