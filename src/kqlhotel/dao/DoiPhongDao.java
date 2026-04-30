package kqlhotel.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import kqlhotel.entity.DoiPhongRoomOption;
import kqlhotel.entity.DoiPhongSearchResult;

public class DoiPhongDao {

    public List<DoiPhongSearchResult> searchBookings(String maDatPhong, String tenKhach, String soDienThoai, String maPhong) {
        List<DoiPhongSearchResult> results = new ArrayList<>();
        String sql =
            "SELECT dp.maDatPhong, kh.maKH, kh.hoTenKH, kh.sdt, kh.CCCD, " +
            "ctdp.maPhong, p.maLoaiPhong, lp.tenLoaiPhong, lp.sucChuaToiDa, " +
            "ctdp.ngayNhanDuKien, ctdp.ngayTraDuKien, ctdp.soLuongNguoiO " +
            "FROM ChiTietDatPhong ctdp " +
            "JOIN DatPhong dp ON dp.maDatPhong = ctdp.maDatPhong " +
            "JOIN KhachHang kh ON kh.maKH = dp.maKH " +
            "JOIN Phong p ON p.maPhong = ctdp.maPhong " +
            "JOIN LoaiPhong lp ON lp.maLoaiPhong = p.maLoaiPhong " +
            "WHERE (? = '' OR dp.maDatPhong LIKE ?) " +
            "AND (? = '' OR kh.hoTenKH LIKE ?) " +
            "AND (? = '' OR kh.sdt LIKE ?) " +
            "AND (? = '' OR ctdp.maPhong LIKE ?) " +
            "ORDER BY dp.ngayNhanDuKien DESC, dp.maDatPhong DESC";

        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maDatPhong);
            ps.setString(2, "%" + maDatPhong + "%");
            ps.setString(3, tenKhach);
            ps.setString(4, "%" + tenKhach + "%");
            ps.setString(5, soDienThoai);
            ps.setString(6, "%" + soDienThoai + "%");
            ps.setString(7, maPhong);
            ps.setString(8, "%" + maPhong + "%");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DoiPhongSearchResult item = new DoiPhongSearchResult();
                    item.setMaChiTietDatPhong(rs.getString("maDatPhong") + ":" + rs.getString("maPhong"));
                    item.setMaDatPhong(rs.getString("maDatPhong"));
                    item.setMaKhachHang(rs.getString("maKH"));
                    item.setTenKhachHang(rs.getString("hoTenKH"));
                    item.setSoDienThoai(rs.getString("sdt"));
                    item.setCccd(rs.getString("CCCD"));
                    item.setMaPhongHienTai(rs.getString("maPhong"));
                    item.setMaLoaiPhongHienTai(rs.getString("maLoaiPhong"));
                    item.setLoaiPhongHienTai(rs.getString("tenLoaiPhong"));
                    Timestamp ngayNhan = rs.getTimestamp("ngayNhanDuKien");
                    Timestamp ngayTra = rs.getTimestamp("ngayTraDuKien");
                    item.setNgayNhan(ngayNhan != null ? ngayNhan.toLocalDateTime() : null);
                    item.setNgayTra(ngayTra != null ? ngayTra.toLocalDateTime() : null);
                    item.setSoLuongNguoiO(rs.getInt("soLuongNguoiO"));
                    item.setSucChuaToiDaPhongHienTai(rs.getInt("sucChuaToiDa"));
                    results.add(item);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return results;
    }

    public List<DoiPhongRoomOption> getAvailableRooms(DoiPhongSearchResult booking) {
        List<DoiPhongRoomOption> rooms = new ArrayList<>();
        String sql =
            "SELECT p.maPhong, p.maLoaiPhong, lp.tenLoaiPhong, lp.sucChuaToiDa, p.tang, p.trangThaiPhong " +
            "FROM Phong p " +
            "JOIN LoaiPhong lp ON lp.maLoaiPhong = p.maLoaiPhong " +
            "WHERE p.trangThaiPhong = 'Trong' " +
            "AND p.maPhong <> ? " +
            "AND lp.sucChuaToiDa >= ? " +
            "ORDER BY CASE WHEN p.maLoaiPhong = ? THEN 0 ELSE 1 END, " +
            "lp.sucChuaToiDa ASC, p.tang ASC, p.maPhong ASC";

        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, booking == null ? "" : booking.getMaPhongHienTai());
            ps.setInt(2, booking == null ? 1 : Math.max(1, booking.getSoLuongNguoiO()));
            ps.setString(3, booking == null ? "" : booking.getMaLoaiPhongHienTai());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DoiPhongRoomOption room = new DoiPhongRoomOption();
                    room.setMaPhong(rs.getString("maPhong"));
                    room.setMaLoaiPhong(rs.getString("maLoaiPhong"));
                    room.setTenLoaiPhong(rs.getString("tenLoaiPhong"));
                    room.setSucChuaToiDa(rs.getInt("sucChuaToiDa"));
                    room.setTang(rs.getInt("tang"));
                    room.setTrangThaiPhong(rs.getString("trangThaiPhong"));
                    rooms.add(room);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return rooms;
    }

    public boolean changeRoom(String maDatPhong, String oldRoom, String newRoom) {
        String updateDetailSql = "UPDATE ChiTietDatPhong SET maPhong = ? WHERE maDatPhong = ? AND maPhong = ?";
        // Quan trọng: Phải cập nhật cả ChiTietHoaDon vì khách có thể đang ở
        String updateInvoiceSql = "UPDATE ChiTietHoaDon SET maPhong = ? WHERE maPhong = ? AND maHD IN (SELECT maHD FROM HoaDon WHERE maDatPhong = ?)";
        
        String updateOldRoomSql = "UPDATE Phong SET trangThaiPhong = 'Trong' WHERE maPhong = ?";
        String updateNewRoomSql = "UPDATE Phong SET trangThaiPhong = 'DangSuDung' WHERE maPhong = ?";

        try (Connection conn = ConnectDB.getConnection()) {
            boolean autoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement updateDetailPs = conn.prepareStatement(updateDetailSql);
                     PreparedStatement updateInvoicePs = conn.prepareStatement(updateInvoiceSql);
                     PreparedStatement updateOldRoomPs = conn.prepareStatement(updateOldRoomSql);
                     PreparedStatement updateNewRoomPs = conn.prepareStatement(updateNewRoomSql)) {
                    
                    updateDetailPs.setString(1, newRoom);
                    updateDetailPs.setString(2, maDatPhong);
                    updateDetailPs.setString(3, oldRoom);
                    int affected = updateDetailPs.executeUpdate();
                    
                    if (affected == 0) {
                        System.err.println("DoiPhongDao: Không tìm thấy booking " + maDatPhong + " với phòng " + oldRoom + " trong ChiTietDatPhong.");
                        conn.rollback();
                        return false;
                    }

                    // Cập nhật ChiTietHoaDon nếu có
                    updateInvoicePs.setString(1, newRoom);
                    updateInvoicePs.setString(2, oldRoom);
                    updateInvoicePs.setString(3, maDatPhong);
                    updateInvoicePs.executeUpdate(); // Có thể = 0 nếu khách chưa check-in, không sao cả

                    updateOldRoomPs.setString(1, oldRoom);
                    updateOldRoomPs.executeUpdate();

                    updateNewRoomPs.setString(1, newRoom);
                    updateNewRoomPs.executeUpdate();
                }

                conn.commit();
                conn.setAutoCommit(autoCommit);
                return true;
            } catch (SQLException ex) {
                conn.rollback();
                conn.setAutoCommit(autoCommit);
                System.err.println("Lỗi SQL khi đổi phòng: " + ex.getMessage());
                ex.printStackTrace();
            }
        } catch (Exception e) {
            System.err.println("Lỗi chung khi đổi phòng: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }
}
