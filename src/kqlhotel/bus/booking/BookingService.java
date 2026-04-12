package kqlhotel.bus.booking;

import java.util.List;
import kqlhotel.bus.booking.model.BookingConfirmationResult;
import kqlhotel.bus.booking.model.BookingSearchRequest;
import kqlhotel.bus.booking.model.BookingSelectionSummary;
import kqlhotel.bus.booking.model.CreateBookingCommand;
import kqlhotel.bus.booking.model.RoomOptionDto;

public interface BookingService {
    List<RoomOptionDto> searchAvailableRooms(BookingSearchRequest request);

    BookingSelectionSummary summarizeSelection(List<RoomOptionDto> selectedRooms, BookingSearchRequest request);

    BookingConfirmationResult createBooking(CreateBookingCommand command);
}
