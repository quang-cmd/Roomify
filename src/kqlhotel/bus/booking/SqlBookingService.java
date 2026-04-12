package kqlhotel.bus.booking;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import kqlhotel.bus.booking.model.BookingConfirmationResult;
import kqlhotel.bus.booking.model.BookingSearchRequest;
import kqlhotel.bus.booking.model.BookingSelectionSummary;
import kqlhotel.bus.booking.model.CreateBookingCommand;
import kqlhotel.bus.booking.model.RoomOptionDto;
import kqlhotel.dao.booking.RoomDao;
import kqlhotel.dao.booking.RoomDaoSqlServer;
import kqlhotel.entity.RoomEntity;

public class SqlBookingService implements BookingService {
    private final RoomDao roomDao;

    public SqlBookingService() {
        this(new RoomDaoSqlServer());
    }

    public SqlBookingService(RoomDao roomDao) {
        this.roomDao = roomDao;
    }

    @Override
    public List<RoomOptionDto> searchAvailableRooms(BookingSearchRequest request) {
        if (request == null || request.getCheckInDate() == null || request.getCheckOutDate() == null) {
            return Collections.emptyList();
        }

        List<RoomEntity> rows = roomDao.findAvailableRooms(
            request.getRoomType(),
            request.getCheckInDate(),
            request.getCheckOutDate(),
            request.getGuests()
        );
        if (rows == null || rows.isEmpty()) {
            return Collections.emptyList();
        }

        List<RoomOptionDto> roomOptions = new java.util.ArrayList<>();
        for (RoomEntity row : rows) {
            String status = row.getAvailableRooms() + "/" + row.getTotalRooms();
            roomOptions.add(new RoomOptionDto(
                row.getRoomType(),
                row.getNightlyPrice(),
                row.getMaxGuests(),
                status,
                row.getAvailableRooms(),
                row.getAmenities()
            ));
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
}