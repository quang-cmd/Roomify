package kqlhotel.dao.booking;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.Normalizer;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import kqlhotel.dao.ConnectDB;
import kqlhotel.entity.RoomEntity;

public class RoomDaoSqlServer implements RoomDao {
    @Override
    public List<String> findAllRoomTypes() {
        List<String> types = new ArrayList<>();
        String sql = "SELECT tenLoaiPhong FROM LoaiPhong ORDER BY tenLoaiPhong";
        try (Connection con = ConnectDB.getInstance().getConnection();
             java.sql.Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                types.add(rs.getString("tenLoaiPhong"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return types;
    }
    @Override
    public List<RoomEntity> findAvailableRooms(String roomType, LocalDate checkInDate, LocalDate checkOutDate, int adults) {
        if (checkInDate == null || checkOutDate == null) {
            return Collections.emptyList();
        }

        Connection connection = getOpenConnection();
        if (connection == null) {
            return Collections.emptyList();
        }

        boolean allRoomTypes = isAllRoomTypes(roomType);
        String roomTypePattern = allRoomTypes ? "%" : "%" + roomType.trim() + "%";
        List<RoomEntity> result = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(buildFindAvailableSql(connection))) {
            statement.setTimestamp(1, Timestamp.valueOf(checkInDate.atStartOfDay()));
            statement.setTimestamp(2, Timestamp.valueOf(checkOutDate.atStartOfDay()));
            statement.setInt(3, allRoomTypes ? 1 : 0);
            statement.setString(4, roomTypePattern);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    RoomEntity row = new RoomEntity();
                    row.setRoomType(rs.getString("tenLoaiPhong"));
                    row.setNightlyPrice(rs.getLong("giaPhong"));
                    row.setMaxGuests(rs.getInt("sucChuaToiDa"));
                    row.setMaxChildren(rs.getInt("soTreEmToiDa"));
                    row.setAvailableRooms(rs.getInt("soPhongTrong"));
                    row.setTotalRooms(rs.getInt("tongSoPhong"));
                    row.setAmenities(splitAmenities(rs.getString("tienNghi")));
                    result.add(row);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            return Collections.emptyList();
        }

        return result;
    }

    private String buildFindAvailableSql(Connection connection) throws SQLException {
        String maxChildrenExpression = resolveMaxChildrenExpression(connection);
        String maxChildrenGroupBy = maxChildrenExpression.startsWith("lp.") ? ", " + maxChildrenExpression : "";

        return "SELECT lp.tenLoaiPhong, lp.giaPhong, lp.sucChuaToiDa, "
            + maxChildrenExpression + " AS soTreEmToiDa, lp.tienNghi, "
            + "SUM(CASE WHEN p.trangThaiPhong <> 'BaoTri' AND ctdp.maPhong IS NULL THEN 1 ELSE 0 END) AS soPhongTrong, "
            + "COUNT(DISTINCT p.maPhong) AS tongSoPhong "
            + "FROM LoaiPhong lp "
            + "JOIN Phong p ON p.maLoaiPhong = lp.maLoaiPhong "
            + "LEFT JOIN ChiTietDatPhong ctdp ON ctdp.maPhong = p.maPhong "
            + "AND ? < ctdp.ngayTraDuKien AND ? > ctdp.ngayNhanDuKien "
            + "AND EXISTS (SELECT 1 FROM HoaDon hd WHERE hd.maDatPhong = ctdp.maDatPhong AND hd.trangThai = 'ChuaThanhToan') "
            + "WHERE (? = 1 OR lp.tenLoaiPhong LIKE ?) "
            + "GROUP BY lp.tenLoaiPhong, lp.giaPhong, lp.sucChuaToiDa" + maxChildrenGroupBy + ", lp.tienNghi "
            + "HAVING SUM(CASE WHEN p.trangThaiPhong <> 'BaoTri' AND ctdp.maPhong IS NULL THEN 1 ELSE 0 END) > 0 "
            + "ORDER BY lp.giaPhong ASC";
    }

    private String resolveMaxChildrenExpression(Connection connection) throws SQLException {
        if (hasColumn(connection, "soTreEmTD")) {
            return "lp.soTreEmTD";
        }
        if (hasColumn(connection, "soTreEmToiDa")) {
            return "lp.soTreEmToiDa";
        }
        return "0";
    }

    private boolean hasColumn(Connection connection, String columnName) throws SQLException {
        String sql = "SELECT COL_LENGTH('dbo.LoaiPhong', '" + columnName + "')";
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {
            return rs.next() && rs.getObject(1) != null;
        }
    }

    private Connection getOpenConnection() {
        Connection connection = ConnectDB.getInstance().getConnection();
        try {
            if (connection == null || connection.isClosed()) {
                ConnectDB.getInstance().connect();
                connection = ConnectDB.getInstance().getConnection();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
        return connection;
    }

    private boolean isAllRoomTypes(String roomType) {
        if (roomType == null || roomType.trim().isEmpty()) {
            return true;
        }

        String normalized = Normalizer.normalize(roomType, Normalizer.Form.NFD)
            .replaceAll("\\p{M}+", "")
            .toLowerCase()
            .trim();

        return "tat ca".equals(normalized) || "all".equals(normalized);
    }

    private List<String> splitAmenities(String rawAmenities) {
        if (rawAmenities == null || rawAmenities.trim().isEmpty()) {
            return defaultAmenities();
        }

        String[] parts = rawAmenities.split(",");
        List<String> amenities = new ArrayList<>();
        for (String part : parts) {
            String value = part.trim();
            if (!value.isEmpty()) {
                amenities.add(value);
            }
        }

        if (amenities.isEmpty()) {
            return defaultAmenities();
        }
        return amenities;
    }

    private List<String> defaultAmenities() {
        List<String> defaults = new ArrayList<>();
        defaults.add("Wifi");
        defaults.add("Minibar");
        return defaults;
    }
}
