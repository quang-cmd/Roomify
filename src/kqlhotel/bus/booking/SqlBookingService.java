package kqlhotel.bus.booking;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import kqlhotel.bus.booking.model.BookingConfirmationResult;
import kqlhotel.bus.booking.model.BookingSearchRequest;
import kqlhotel.bus.booking.model.BookingSelectionSummary;
import kqlhotel.bus.booking.model.CreateBookingCommand;
import kqlhotel.bus.booking.model.RoomOptionDto;
import kqlhotel.dao.ConnectDB;

public class SqlBookingService implements BookingService {
    private static final String SEARCH_AVAILABLE_SQL =
        "SELECT lp.tenLoaiPhong, lp.giaPhong, lp.sucChuaToiDa, lp.tienNghi, " +
        "SUM(CASE WHEN p.trangThaiPhong = 'Trong' AND ctdp.maCTDP IS NULL THEN 1 ELSE 0 END) AS soPhongTrong, " +
        "COUNT(*) AS tongSoPhong " +
        "FROM LoaiPhong lp " +
        "JOIN Phong p ON p.maLoaiPhong = lp.maLoaiPhong " +
        "LEFT JOIN ChiTietDatPhong ctdp ON ctdp.maPhong = p.maPhong " +
        "AND ? < ctdp.ngayTraDuKien AND ? > ctdp.ngayNhanDuKien " +
        "WHERE lp.sucChuaToiDa >= ? " +
        "AND (? = 1 OR lp.tenLoaiPhong LIKE ?) " +
        "GROUP BY lp.tenLoaiPhong, lp.giaPhong, lp.sucChuaToiDa, lp.tienNghi " +
        "HAVING SUM(CASE WHEN p.trangThaiPhong = 'Trong' AND ctdp.maCTDP IS NULL THEN 1 ELSE 0 END) > 0 " +
        "ORDER BY lp.giaPhong ASC";

    @Override
    public List<RoomOptionDto> searchAvailableRooms(BookingSearchRequest request) {
        if (request == null || request.getCheckInDate() == null || request.getCheckOutDate() == null) {
            return Collections.emptyList();
        }

        Connection connection = getOpenConnection();
        if (connection == null) {
            return Collections.emptyList();
        }

        List<RoomOptionDto> roomOptions = new ArrayList<>();
        boolean allRoomTypes = isAllRoomTypes(request.getRoomType());
        String roomTypePattern = allRoomTypes ? "%" : "%" + request.getRoomType().trim() + "%";

        try (PreparedStatement statement = connection.prepareStatement(SEARCH_AVAILABLE_SQL)) {
            statement.setTimestamp(1, Timestamp.valueOf(request.getCheckInDate().atStartOfDay()));
            statement.setTimestamp(2, Timestamp.valueOf(request.getCheckOutDate().atStartOfDay()));
            statement.setInt(3, request.getGuests());
            statement.setInt(4, allRoomTypes ? 1 : 0);
            statement.setString(5, roomTypePattern);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    String roomType = rs.getString("tenLoaiPhong");
                    long nightlyPrice = rs.getLong("giaPhong");
                    int maxGuests = rs.getInt("sucChuaToiDa");
                    int availableRooms = rs.getInt("soPhongTrong");
                    int totalRooms = rs.getInt("tongSoPhong");
                    String status = availableRooms + "/" + totalRooms;
                    List<String> amenities = splitAmenities(rs.getString("tienNghi"));

                    roomOptions.add(new RoomOptionDto(roomType, nightlyPrice, maxGuests, status, availableRooms, amenities));
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            return Collections.emptyList();
        }

        return roomOptions;
    }

    @Override
    public BookingSelectionSummary summarizeSelection(List<RoomOptionDto> selectedRooms, BookingSearchRequest request) {
        if (selectedRooms == null || selectedRooms.isEmpty() || request == null) {
            return new BookingSelectionSummary(0, 0, 0);
        }

        LocalDate checkInDate = request.getCheckInDate();
        LocalDate checkOutDate = request.getCheckOutDate();
        int nights = (int) Math.max(1, ChronoUnit.DAYS.between(checkInDate, checkOutDate));

        long roomTotalPerNight = 0;
        for (RoomOptionDto selectedRoom : selectedRooms) {
            roomTotalPerNight += selectedRoom.getNightlyPrice();
        }

        long totalAmount = roomTotalPerNight * nights;
        return new BookingSelectionSummary(selectedRooms.size(), nights, totalAmount);
    }

    @Override
    public BookingConfirmationResult createBooking(CreateBookingCommand command) {
        return new BookingConfirmationResult(false, null, "Chuc nang luu DatPhong/HoaDon dang duoc hoan thien. Tam thoi da ho tro doc du lieu phong tu CSDL.");
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
}