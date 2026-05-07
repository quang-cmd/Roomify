package kqlhotel.bus.booking;

import java.time.LocalDate;
import java.util.List;

public class CreateBookingCommand {

    public static final double DEPOSIT_RATIO = 0.30;
    public static final double FULL_RATIO    = 1.0;

    private final LocalDate checkInDate;
    private final LocalDate checkOutDate;
    private final int totalGuests;
    private final List<GuestInfoDto> guestInfos;
    private final List<RoomOptionDto> selectedRooms;
    private final long totalAmount;
    private final double paymentRatio;
    private final String paymentMethod;
    private final String paymentReference;

    public CreateBookingCommand(
        LocalDate checkInDate,
        LocalDate checkOutDate,
        int totalGuests,
        List<GuestInfoDto> guestInfos,
        List<RoomOptionDto> selectedRooms
    ) {
        this(checkInDate, checkOutDate, totalGuests, guestInfos, selectedRooms, 0L, 1.0, "TienMat", "");
    }

    public CreateBookingCommand(
        LocalDate checkInDate,
        LocalDate checkOutDate,
        int totalGuests,
        List<GuestInfoDto> guestInfos,
        List<RoomOptionDto> selectedRooms,
        long totalAmount,
        double paymentRatio,
        String paymentMethod,
        String paymentReference
    ) {
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.totalGuests = totalGuests;
        this.guestInfos = guestInfos;
        this.selectedRooms = selectedRooms;
        this.totalAmount = totalAmount;
        this.paymentRatio = paymentRatio;
        this.paymentMethod = paymentMethod == null ? "TienMat" : paymentMethod;
        this.paymentReference = paymentReference == null ? "" : paymentReference;
    }

    public long getTotalAmount() {
        return totalAmount;
    }

    public double getPaymentRatio() {
        return paymentRatio;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public String getPaymentReference() {
        return paymentReference;
    }

    public long getPaymentAmount() {
        return Math.round(totalAmount * paymentRatio);
    }

    public boolean isFullyPaid() {
        return paymentRatio >= 1.0;
    }

    public LocalDate getCheckInDate() {
        return checkInDate;
    }

    public LocalDate getCheckOutDate() {
        return checkOutDate;
    }

    public int getTotalGuests() {
        return totalGuests;
    }

    public List<GuestInfoDto> getGuestInfos() {
        return guestInfos;
    }

    public String getCustomerName() {
        return guestInfos != null && !guestInfos.isEmpty() ? guestInfos.get(0).getFullName() : null;
    }

    public String getCustomerPhone() {
        return guestInfos != null && !guestInfos.isEmpty() ? guestInfos.get(0).getSdt() : null;
    }

    public String getCustomerIdNo() {
        return guestInfos != null && !guestInfos.isEmpty() ? guestInfos.get(0).getIdNo() : null;
    }

    public List<RoomOptionDto> getSelectedRooms() {
        return selectedRooms;
    }
}
