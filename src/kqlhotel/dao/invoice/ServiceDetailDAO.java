package kqlhotel.dao.invoice;

import kqlhotel.dao.ConnectDB;
import kqlhotel.entity.ServiceDetail;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceDetailDAO {

    public List<ServiceDetail> getByInvoice(String maHD) {
        List<ServiceDetail> list = new ArrayList<>();
        try {
            Connection con = ConnectDB.getInstance().getConnection();
            String sql = "SELECT ctdv.*, dv.tenDV FROM ChiTietDichVu ctdv " +
                    "JOIN DichVu dv ON ctdv.maDV = dv.maDV WHERE ctdv.maHD = ?";
            PreparedStatement pstmt = con.prepareStatement(sql);
            pstmt.setString(1, maHD);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                ServiceDetail ct = new ServiceDetail();
                ct.setMaCTDV(rs.getString("maCTDV"));
                ct.setSoLuong(rs.getInt("soLuong"));
                ct.setDonGia(rs.getDouble("donGia"));
                ct.setThanhTien(rs.getDouble("thanhTien"));
                ct.setGhiChu(rs.getString("ghiChu"));
                ct.setMaDV(rs.getString("maDV"));
                ct.setMaHD(rs.getString("maHD"));
                // Lưu tenDV vào ghiChu nếu chưa có ghi chú
                if (ct.getGhiChu() == null || ct.getGhiChu().isEmpty()) {
                    ct.setGhiChu(rs.getString("tenDV"));
                }
                list.add(ct);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Tạo mã CTDV tự động theo format CTDVxxxx
     */
    private String generateMaCTDV(Connection con) throws SQLException {
        String sql = "SELECT MAX(maCTDV) FROM ChiTietDichVu";
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        if (rs.next() && rs.getString(1) != null) {
            String last = rs.getString(1); // e.g. "CTDV0005"
            try {
                int num = Integer.parseInt(last.replaceAll("[^0-9]", ""));
                return String.format("CTDV%04d", num + 1);
            } catch (Exception e) {
                // fallback
            }
        }
        return "CTDV0001";
    }

    /**
     * Thêm chi tiết dịch vụ vào DB và cập nhật tienDichVu + tienThue + tongTienThanhToan trên HoaDon
     */
    public boolean insert(ServiceDetail sd) {
        try {
            Connection con = ConnectDB.getInstance().getConnection();
            // 1. Generate maCTDV
            String maCTDV = generateMaCTDV(con);
            sd.setMaCTDV(maCTDV);

            // 2. Insert ChiTietDichVu
            String sqlInsert = "INSERT INTO ChiTietDichVu (maCTDV, maHD, maDV, soLuong, donGia, thanhTien, ghiChu) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement ps = con.prepareStatement(sqlInsert);
            ps.setString(1, maCTDV);
            ps.setString(2, sd.getMaHD());
            ps.setString(3, sd.getMaDV());
            ps.setInt(4, sd.getSoLuong());
            ps.setDouble(5, sd.getDonGia());
            ps.setDouble(6, sd.getThanhTien());
            ps.setString(7, sd.getGhiChu());
            ps.executeUpdate();

            // 3. Cập nhật HoaDon: chỉ cộng tiền dịch vụ.
            // Thuế VAT của hệ thống được tính theo tiền phòng thực tế, không tính trên dịch vụ.
            String sqlUpdate = "UPDATE HoaDon SET " +
                    "tienDichVu = tienDichVu + ?, " +
                    "tongTienThanhToan = tienPhong + (tienDichVu + ?) + tienThue - tienKhuyenMai + phiDoiPhong " +
                    "WHERE maHD = ?";
            PreparedStatement psUpd = con.prepareStatement(sqlUpdate);
            psUpd.setDouble(1, sd.getThanhTien());
            psUpd.setDouble(2, sd.getThanhTien());
            psUpd.setString(3, sd.getMaHD());
            psUpd.executeUpdate();

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Xóa chi tiết dịch vụ và cập nhật lại tổng tiền hóa đơn
     */
    public boolean delete(String maCTDV) {
        try {
            Connection con = ConnectDB.getInstance().getConnection();

            // Lấy thông tin trước khi xóa
            String sqlGet = "SELECT maHD, thanhTien FROM ChiTietDichVu WHERE maCTDV = ?";
            PreparedStatement psGet = con.prepareStatement(sqlGet);
            psGet.setString(1, maCTDV);
            ResultSet rs = psGet.executeQuery();
            if (!rs.next()) return false;
            String maHD = rs.getString("maHD");
            double thanhTien = rs.getDouble("thanhTien");

            // Xóa
            String sqlDel = "DELETE FROM ChiTietDichVu WHERE maCTDV = ?";
            PreparedStatement psDel = con.prepareStatement(sqlDel);
            psDel.setString(1, maCTDV);
            psDel.executeUpdate();

            // Cập nhật HoaDon: chỉ trừ tiền dịch vụ.
            // Thuế VAT của hệ thống được tính theo tiền phòng thực tế, không tính trên dịch vụ.
            String sqlUpd = "UPDATE HoaDon SET " +
                    "tienDichVu = tienDichVu - ?, " +
                    "tongTienThanhToan = tienPhong + (tienDichVu - ?) + tienThue - tienKhuyenMai + phiDoiPhong " +
                    "WHERE maHD = ?";
            PreparedStatement psUpd = con.prepareStatement(sqlUpd);
            psUpd.setDouble(1, thanhTien);
            psUpd.setDouble(2, thanhTien);
            psUpd.setString(3, maHD);
            psUpd.executeUpdate();

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
