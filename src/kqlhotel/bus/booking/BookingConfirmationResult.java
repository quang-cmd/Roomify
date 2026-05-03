package kqlhotel.bus.booking;

public class BookingConfirmationResult {
    private final boolean success;
    private final String bookingCode;
    private final String message;

    public BookingConfirmationResult(boolean success, String bookingCode, String message) {
        this.success = success;
        this.bookingCode = bookingCode;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getBookingCode() {
        return bookingCode;
    }

    public String getMessage() {
        return message;
    }
}
