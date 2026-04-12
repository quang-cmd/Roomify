package kqlhotel.bus.booking;

public final class BookingServiceProvider {
    private static BookingService service = new PendingBookingService();

    private BookingServiceProvider() {
    }

    public static BookingService get() {
        return service;
    }

    public static void set(BookingService bookingService) {
        if (bookingService == null) {
            throw new IllegalArgumentException("bookingService must not be null");
        }
        service = bookingService;
    }
}
