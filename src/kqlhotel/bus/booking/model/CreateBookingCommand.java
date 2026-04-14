package kqlhotel.bus.booking.model;

import java.time.LocalDate;
import java.util.List;

public class CreateBookingCommand {
    private final LocalDate checkInDate;
    private final LocalDate checkOutDate;
    private final int totalGuests;
    private final List<GuestInfoDto> guestInfos;
    private final List<RoomOptionDto> selectedRooms;

    public CreateBookingCommand(
        LocalDate checkInDate,
        LocalDate checkOutDate,
        int totalGuests,
        List<GuestInfoDto> guestInfos,
        List<RoomOptionDto> selectedRooms
    ) {
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.totalGuests = totalGuests;
        this.guestInfos = guestInfos;
        this.selectedRooms = selectedRooms;
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
