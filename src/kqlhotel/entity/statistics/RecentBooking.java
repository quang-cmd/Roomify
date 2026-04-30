package kqlhotel.entity.statistics;

import java.time.LocalDateTime;

/** 1 dòng trong list "Đặt phòng gần đây" của dashboard thống kê. */
public class RecentBooking {
    private final String roomCode;
    private final String guestName;
    private final String roomType;
    private final String status;
    private final LocalDateTime bookingDate;

    public RecentBooking(String roomCode, String guestName, String roomType,
                         String status, LocalDateTime bookingDate) {
        this.roomCode = roomCode;
        this.guestName = guestName;
        this.roomType = roomType;
        this.status = status;
        this.bookingDate = bookingDate;
    }

    public String        getRoomCode()    { return roomCode; }
    public String        getGuestName()   { return guestName; }
    public String        getRoomType()    { return roomType; }
    public String        getStatus()      { return status; }
    public LocalDateTime getBookingDate() { return bookingDate; }
}
