package kqlhotel.bus.booking;

import java.util.List;
import kqlhotel.bus.booking.BookingConfirmationResult;
import kqlhotel.bus.booking.BookingSearchRequest;
import kqlhotel.bus.booking.BookingSelectionSummary;
import kqlhotel.bus.booking.CreateBookingCommand;
import kqlhotel.bus.booking.RoomOptionDto;

public interface BookingService {
    List<RoomOptionDto> searchAvailableRooms(BookingSearchRequest request);

    BookingSelectionSummary summarizeSelection(List<RoomOptionDto> selectedRooms, BookingSearchRequest request);

    BookingConfirmationResult createBooking(CreateBookingCommand command);
}
