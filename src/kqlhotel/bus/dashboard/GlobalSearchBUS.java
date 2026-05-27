package kqlhotel.bus.dashboard;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import kqlhotel.dao.ConnectDB;
import kqlhotel.gui.Permission;

/**
 * BUS gom toàn bộ logic tìm kiếm toàn cục.
 *
 * V1 chỉ trả về kết quả để điều hướng sang đúng màn hình, chưa tự chọn đúng
 * bản ghi trong từng panel. Vì vậy mỗi query chỉ lấy dữ liệu ngắn gọn đủ để
 * hiển thị title/subtitle trong popup.
 */
public class GlobalSearchBUS {
    // Giới hạn mỗi nhóm để popup không quá dài và tránh query nhiều dữ liệu.
    private static final int LIMIT_PER_GROUP = 5;

    public List<SearchResult> search(String keyword) {
        String normalized = keyword == null ? "" : keyword.trim();

        // Chỉ bắt đầu tìm từ 2 ký tự để tránh những truy vấn quá rộng như "a".
        if (normalized.length() < 2) {
            return List.of();
        }

        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) {
            return List.of();
        }

        List<SearchResult> results = new ArrayList<>();
        String like = "%" + normalized + "%";

        try {
            // Role-aware search: nhóm nào user không có quyền mở thì không đưa
            // vào kết quả. Nhờ vậy popup không gợi ý màn hình bị cấm quyền.
            if (Permission.canAccess("customers")) {
                searchCustomers(con, like, results);
            }
            if (Permission.canAccess("booking")) {
                searchBookings(con, like, results);
            }
            if (Permission.canAccess("room-management")) {
                searchRooms(con, like, results);
            }
            if (Permission.canAccess("invoices")) {
                searchInvoices(con, like, results);
            }
            if (Permission.canAccess("staff")) {
                searchStaff(con, like, results);
            }
        } catch (SQLException ex) {
            // Search là tính năng phụ trợ, nên lỗi DB không được làm văng app.
            // UI sẽ nhận danh sách rỗng và hiển thị thông báo nhẹ.
            ex.printStackTrace();
            return List.of();
        }

        return results;
    }

    private void searchCustomers(Connection con, String like, List<SearchResult> out) throws SQLException {
        // Khách hàng được nhận diện chủ yếu bằng tên, số điện thoại hoặc CCCD.
        String sql = """
            SELECT TOP (?) maKH, hoTenKH, sdt, CCCD
            FROM KhachHang
            WHERE hoTenKH LIKE ? OR sdt LIKE ? OR CCCD LIKE ?
            ORDER BY hoTenKH
        """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, LIMIT_PER_GROUP);
            ps.setString(2, like);
            ps.setString(3, like);
            ps.setString(4, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(new SearchResult(
                        "Khách hàng",
                        safe(rs.getString("hoTenKH")),
                        rs.getString("maKH") + " · SĐT: " + safe(rs.getString("sdt")) + " · CCCD: " + safe(rs.getString("CCCD")),
                        "customers",
                        "customers"
                    ));
                }
            }
        }
    }

    private void searchBookings(Connection con, String like, List<SearchResult> out) throws SQLException {
        // Đặt phòng tìm theo mã booking hoặc thông tin khách đại diện.
        String sql = """
            SELECT TOP (?) dp.maDatPhong, dp.trangThaiDatPhong, kh.hoTenKH, kh.sdt
            FROM DatPhong dp
            JOIN KhachHang kh ON kh.maKH = dp.maKH
            WHERE dp.maDatPhong LIKE ? OR kh.hoTenKH LIKE ? OR kh.sdt LIKE ?
            ORDER BY dp.ngayDat DESC
        """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, LIMIT_PER_GROUP);
            ps.setString(2, like);
            ps.setString(3, like);
            ps.setString(4, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(new SearchResult(
                        "Đặt phòng",
                        rs.getString("maDatPhong") + " · " + safe(rs.getString("hoTenKH")),
                        "SĐT: " + safe(rs.getString("sdt")) + " · Trạng thái: " + displayBookingStatus(rs.getString("trangThaiDatPhong")),
                        "booking",
                        "booking"
                    ));
                }
            }
        }
    }

    private void searchRooms(Connection con, String like, List<SearchResult> out) throws SQLException {
        // V1 chỉ tìm phòng theo mã phòng để tránh kết quả quá rộng.
        String sql = """
            SELECT TOP (?) p.maPhong, p.tang, p.trangThaiPhong, lp.tenLoaiPhong
            FROM Phong p
            JOIN LoaiPhong lp ON lp.maLoaiPhong = p.maLoaiPhong
            WHERE p.maPhong LIKE ?
            ORDER BY p.maPhong
        """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, LIMIT_PER_GROUP);
            ps.setString(2, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(new SearchResult(
                        "Phòng",
                        rs.getString("maPhong") + " · " + safe(rs.getString("tenLoaiPhong")),
                        "Tầng " + rs.getInt("tang") + " · Trạng thái: " + displayRoomStatus(rs.getString("trangThaiPhong")),
                        "room-management",
                        "room"
                    ));
                }
            }
        }
    }

    private void searchInvoices(Connection con, String like, List<SearchResult> out) throws SQLException {
        // Hóa đơn tìm theo mã hóa đơn; click chỉ mở màn hình Hóa đơn.
        String sql = """
            SELECT TOP (?) hd.maHD, hd.trangThai, hd.tongTienThanhToan, kh.hoTenKH
            FROM HoaDon hd
            JOIN KhachHang kh ON kh.maKH = hd.maKH
            WHERE hd.maHD LIKE ?
            ORDER BY hd.ngayLapHD DESC
        """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, LIMIT_PER_GROUP);
            ps.setString(2, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(new SearchResult(
                        "Hóa đơn",
                        rs.getString("maHD") + " · " + safe(rs.getString("hoTenKH")),
                        formatMoney(rs.getDouble("tongTienThanhToan")) + " · Trạng thái: " + displayInvoiceStatus(rs.getString("trangThai")),
                        "invoices",
                        "invoices"
                    ));
                }
            }
        }
    }

    private void searchStaff(Connection con, String like, List<SearchResult> out) throws SQLException {
        // Nhân sự chỉ được query khi Permission cho phép route "staff".
        String sql = """
            SELECT TOP (?) maNV, hoTenNV, sdt, tenDangNhap
            FROM NhanVien
            WHERE maNV LIKE ? OR hoTenNV LIKE ?
            ORDER BY hoTenNV
        """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, LIMIT_PER_GROUP);
            ps.setString(2, like);
            ps.setString(3, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(new SearchResult(
                        "Nhân sự",
                        rs.getString("maNV") + " · " + safe(rs.getString("hoTenNV")),
                        "SĐT: " + safe(rs.getString("sdt")) + " · Tài khoản: " + safe(rs.getString("tenDangNhap")),
                        "staff",
                        "staff"
                    ));
                }
            }
        }
    }

    private String displayBookingStatus(String status) {
        // Map mã trạng thái trong DB sang nhãn tiếng Việt dễ đọc trên popup.
        if ("DaDat".equals(status)) return "Đã đặt";
        if ("DangO".equals(status)) return "Đang ở";
        if ("DaTra".equals(status)) return "Đã trả";
        if ("DaHuy".equals(status)) return "Đã hủy";
        return safe(status);
    }

    private String displayRoomStatus(String status) {
        if ("Trong".equals(status)) return "Trống";
        if ("DangSuDung".equals(status)) return "Đang sử dụng";
        if ("BaoTri".equals(status)) return "Bảo trì";
        return safe(status);
    }

    private String displayInvoiceStatus(String status) {
        if ("ChuaThanhToan".equals(status)) return "Chưa thanh toán";
        if ("DaThanhToan".equals(status)) return "Đã thanh toán";
        if ("DaHuy".equals(status)) return "Đã hủy";
        return safe(status);
    }

    private String formatMoney(double value) {
        return String.format("%,.0f đ", value);
    }

    private String safe(String value) {
        // Chuẩn hóa giá trị null/rỗng để popup không hiện chữ "null".
        return value == null || value.trim().isEmpty() ? "-" : value.trim();
    }
}
