package kqlhotel.bus.booking;

import java.time.LocalDate;

public class BookingSearchRequest {
    private final String roomType;
    private final LocalDate checkInDate;
    private final LocalDate checkOutDate;
    private final int guests;

    public BookingSearchRequest(String roomType, LocalDate checkInDate, LocalDate checkOutDate, int guests) {
        this.roomType = roomType;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.guests = guests;
    }

    public String getRoomType() {
        return roomType;
    }

    public LocalDate getCheckInDate() {
        return checkInDate;
    }

    public LocalDate getCheckOutDate() {
        return checkOutDate;
    }

    public int getGuests() {
        return guests;
    }
}
