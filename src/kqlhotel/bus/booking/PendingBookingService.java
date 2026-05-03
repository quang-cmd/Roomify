package kqlhotel.bus.booking;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import kqlhotel.bus.booking.BookingConfirmationResult;
import kqlhotel.bus.booking.BookingSearchRequest;
import kqlhotel.bus.booking.BookingSelectionSummary;
import kqlhotel.bus.booking.CreateBookingCommand;
import kqlhotel.bus.booking.RoomOptionDto;

public class PendingBookingService implements BookingService {
    @Override
    public List<RoomOptionDto> searchAvailableRooms(BookingSearchRequest request) {
        return new ArrayList<>();
    }

    @Override
    public BookingSelectionSummary summarizeSelection(List<RoomOptionDto> selectedRooms, BookingSearchRequest request) {
        if (selectedRooms == null || selectedRooms.isEmpty() || request == null) {
            return new BookingSelectionSummary(0, 0, 0);
        }

        int nights = (int) Math.max(1, ChronoUnit.DAYS.between(request.getCheckInDate(), request.getCheckOutDate()));
        long roomTotalPerNight = selectedRooms.stream().mapToLong(RoomOptionDto::getNightlyPrice).sum();
        long totalAmount = roomTotalPerNight * nights;
        return new BookingSelectionSummary(selectedRooms.size(), nights, totalAmount);
    }

    @Override
    public BookingConfirmationResult createBooking(CreateBookingCommand command) {
        return new BookingConfirmationResult(false, null, "Chua ket noi CSDL dat phong. Vui long cau hinh SQL de tiep tuc.");
    }
}
