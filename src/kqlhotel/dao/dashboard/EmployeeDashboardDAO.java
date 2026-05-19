package kqlhotel.dao.dashboard;

import kqlhotel.dao.ConnectDB;
import kqlhotel.dto.dashboard.RoomScheduleDTO;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class EmployeeDashboardDAO {

    public int countCheckInBookingsToday(LocalDate date) {
        String sql = """
            SELECT COUNT(DISTINCT dp.maDatPhong)
            FROM DatPhong dp
            JOIN ChiTietDatPhong ctdp ON dp.maDatPhong = ctdp.maDatPhong
            WHERE CAST(ctdp.ngayNhanDuKien AS DATE) = ?
              AND dp.trangThaiDatPhong = 'DaDat'
        """;

        return count(sql, Date.valueOf(date));
    }

    public int countCheckInRoomsToday(LocalDate date) {
        String sql = """
            SELECT COUNT(*)
            FROM DatPhong dp
            JOIN ChiTietDatPhong ctdp ON dp.maDatPhong = ctdp.maDatPhong
            WHERE CAST(ctdp.ngayNhanDuKien AS DATE) = ?
              AND dp.trangThaiDatPhong = 'DaDat'
        """;

        return count(sql, Date.valueOf(date));
    }

    public int countCheckOutRoomsToday(LocalDate date) {
        String sql = """
        SELECT COUNT(*)
        FROM HoaDon hd
        JOIN ChiTietHoaDon cthd ON hd.maHD = cthd.maHD
        WHERE CAST(cthd.ngayTraPhong AS DATE) = ?
          AND cthd.ngayNhanPhong IS NOT NULL
          AND cthd.ngayTraThucTe IS NULL
          AND hd.trangThai <> 'DaHuy'
    """;

        return count(sql, Date.valueOf(date));
    }

    public int countNeedPayment() {
        String sql = """
            SELECT COUNT(*)
            FROM HoaDon hd
            OUTER APPLY (
                SELECT ISNULL(SUM(tt.soTienTT), 0) AS daThanhToan
                FROM ThanhToan tt
                WHERE tt.maHD = hd.maHD
                  AND tt.trangThaiTT = 'ThanhToanThanhCong'
            ) paid
            WHERE hd.trangThai <> 'DaHuy'
              AND paid.daThanhToan < hd.tongTienThanhToan
        """;

        return count(sql);
    }

    public List<RoomScheduleDTO> getTodayCheckInRooms(LocalDate date) {
        String sql = """
            SELECT
                ctdp.maPhong,
                ISNULL(kh.hoTenKH, N'Khách hàng') AS tenKhach,
                CONVERT(VARCHAR(5), ctdp.ngayNhanDuKien, 108) AS gio,
                dp.maDatPhong AS maThamChieu
            FROM DatPhong dp
            JOIN ChiTietDatPhong ctdp ON dp.maDatPhong = ctdp.maDatPhong
            LEFT JOIN KhachHang kh ON dp.maKH = kh.maKH
            WHERE CAST(ctdp.ngayNhanDuKien AS DATE) = ?
              AND dp.trangThaiDatPhong = 'DaDat'
            ORDER BY ctdp.ngayNhanDuKien ASC, ctdp.maPhong ASC
        """;

        return queryRoomSchedule(sql, Date.valueOf(date), "Chờ nhận phòng");
    }

    public List<RoomScheduleDTO> getTodayCheckOutRooms(LocalDate date) {
        String sql = """
        SELECT
            cthd.maPhong,
            ISNULL(kh.hoTenKH, N'Khách hàng') AS tenKhach,
            CONVERT(VARCHAR(5), cthd.ngayTraPhong, 108) AS gio,
            hd.maHD AS maThamChieu
        FROM HoaDon hd
        JOIN ChiTietHoaDon cthd ON hd.maHD = cthd.maHD
        LEFT JOIN KhachHang kh ON hd.maKH = kh.maKH
        WHERE CAST(cthd.ngayTraPhong AS DATE) = ?
          AND cthd.ngayNhanPhong IS NOT NULL
          AND cthd.ngayTraThucTe IS NULL
          AND hd.trangThai <> 'DaHuy'
        ORDER BY cthd.ngayTraPhong ASC, cthd.maPhong ASC
    """;

        return queryRoomSchedule(sql, Date.valueOf(date), "Chờ trả phòng");
    }

    private List<RoomScheduleDTO> queryRoomSchedule(String sql, Object param, String note) {
        List<RoomScheduleDTO> list = new ArrayList<>();

        for (int attempt = 1; attempt <= 2; attempt++) {
            try {
                Connection con = ConnectDB.getConnection();

                if (con == null || con.isClosed()) {
                    ConnectDB.getInstance().connect();
                    con = ConnectDB.getConnection();
                }

                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setObject(1, param);

                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            list.add(new RoomScheduleDTO(
                                    rs.getString("maPhong"),
                                    rs.getString("tenKhach"),
                                    rs.getString("gio"),
                                    note + " · " + rs.getString("maThamChieu")
                            ));
                        }
                    }
                }

                return list;
            } catch (Exception e) {
                if (attempt == 1) {
                    try {
                        ConnectDB.getInstance().connect();
                    } catch (Exception ignored) {}
                    continue;
                }

                System.err.println("EmployeeDashboardDAO queryRoomSchedule error: " + e.getMessage());
            }
        }

        return list;
    }

    private int count(String sql, Object... params) {
        for (int attempt = 1; attempt <= 2; attempt++) {
            try {
                Connection con = ConnectDB.getConnection();

                if (con == null || con.isClosed()) {
                    ConnectDB.getInstance().connect();
                    con = ConnectDB.getConnection();
                }

                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    for (int i = 0; i < params.length; i++) {
                        ps.setObject(i + 1, params[i]);
                    }

                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            return rs.getInt(1);
                        }
                    }
                }

                return 0;
            } catch (Exception e) {
                if (attempt == 1) {
                    try {
                        ConnectDB.getInstance().connect();
                    } catch (Exception ignored) {}
                    continue;
                }

                System.err.println("EmployeeDashboardDAO count error: " + e.getMessage());
            }
        }

        return 0;
    }
}