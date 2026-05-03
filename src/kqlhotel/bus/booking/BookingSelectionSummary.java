package kqlhotel.bus.booking;

public class BookingSelectionSummary {
    private final int selectedRooms;
    private final int nights;
    private final long totalAmount;

    public BookingSelectionSummary(int selectedRooms, int nights, long totalAmount) {
        this.selectedRooms = selectedRooms;
        this.nights = nights;
        this.totalAmount = totalAmount;
    }

    public int getSelectedRooms() {
        return selectedRooms;
    }

    public int getNights() {
        return nights;
    }

    public long getTotalAmount() {
        return totalAmount;
    }
}
