package kqlhotel.entity;

public class BookingRoomEntity {
    private String bookingId;
    private String roomId;
    private long nightlyRate;

    public String getBookingId() {
        return bookingId;
    }

    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public long getNightlyRate() {
        return nightlyRate;
    }

    public void setNightlyRate(long nightlyRate) {
        this.nightlyRate = nightlyRate;
    }
}
