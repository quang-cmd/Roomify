package kqlhotel.dao.booking;

import java.util.List;
import kqlhotel.entity.BookingEntity;
import kqlhotel.entity.BookingRoomEntity;

public interface BookingDao {
    String createBooking(BookingEntity booking);

    void createBookingRooms(List<BookingRoomEntity> bookingRooms);
}
