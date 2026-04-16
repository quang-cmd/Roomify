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
            Connection con = ConnectDB.getInstance().getConnection();
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
}