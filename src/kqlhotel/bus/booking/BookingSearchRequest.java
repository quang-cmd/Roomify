package kqlhotel.bus.booking;

import java.time.LocalDate;

public class BookingSearchRequest {
    private final String roomType;
    private final LocalDate checkInDate;
    private final LocalDate checkOutDate;
    private final int adults;
    private final int children;

    public BookingSearchRequest(String roomType, LocalDate checkInDate, LocalDate checkOutDate, int adults, int children) {
        this.roomType = roomType;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.adults = adults;
        this.children = children;
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

    public int getAdults() {
        return adults;
    }

    public int getChildren() {
        return children;
    }
}
