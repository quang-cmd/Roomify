package kqlhotel.bus.booking;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import kqlhotel.bus.booking.BookingConfirmationResult;
import kqlhotel.bus.booking.BookingSearchRequest;
import kqlhotel.bus.booking.BookingSelectionSummary;
import kqlhotel.bus.booking.CreateBookingCommand;
import kqlhotel.bus.booking.GuestInfoDto;
import kqlhotel.bus.booking.RoomOptionDto;

public class MockBookingService implements BookingService {
    private final List<RoomOptionDto> mockRooms = Arrays.asList(
        new RoomOptionDto("Deluxe", 1_200_000L, 2, "2/5 trống", 2, Arrays.asList("Wifi", "Minibar", "Ban công")),
        new RoomOptionDto("Grand Premium 1", 2_200_000L, 2, "2/4 trống", 2, Arrays.asList("Wifi", "Minibar", "Ban công")),
        new RoomOptionDto("Grand Premium 2", 3_200_000L, 2, "2/3 trống", 2, Arrays.asList("Wifi", "Minibar", "Ban công")),
        new RoomOptionDto("Suite", 5_500_000L, 2, "2/3 trống", 2, Arrays.asList("Wifi", "Minibar", "Phòng khách"))
    );

    @Override
    public List<RoomOptionDto> searchAvailableRooms(BookingSearchRequest request) {
        if (request == null) {
            return new ArrayList<>();
        }

        return mockRooms.stream()
            .filter(room -> room.getAvailableRooms() > 0)
            .filter(room -> "Tất cả".equals(request.getRoomType())
                || room.getRoomType().equalsIgnoreCase(request.getRoomType())
                || room.getRoomType().startsWith(request.getRoomType()))
            .collect(Collectors.toList());
    }

    @Override
    public BookingSelectionSummary summarizeSelection(List<RoomOptionDto> selectedRooms, BookingSearchRequest request) {
        if (selectedRooms == null || selectedRooms.isEmpty() || request == null) {
            return new BookingSelectionSummary(0, 0, 0);
        }

        int nights = (int) Math.max(1, ChronoUnit.DAYS.between(request.getCheckInDate(), request.getCheckOutDate()));
        long roomTotalPerNight = selectedRooms.stream().mapToLong(RoomOptionDto::getNightlyPrice).sum();
        long totalAmount = roomTotalPerNight * nights;

        return new BookingSelectionSummary(selectedRooms.size(), nights, totalAmount);
    }

    @Override
    public BookingConfirmationResult createBooking(CreateBookingCommand command) {
        if (command == null || command.getSelectedRooms() == null || command.getSelectedRooms().isEmpty()) {
            return new BookingConfirmationResult(false, null, "Vui lòng chọn ít nhất 1 phòng.");
        }

        if (command.getGuestInfos() == null || command.getGuestInfos().size() != command.getTotalGuests()) {
            return new BookingConfirmationResult(false, null, "Vui lòng nhập đủ thông tin cho tất cả khách.");
        }

        for (GuestInfoDto guest : command.getGuestInfos()) {
            if (guest == null || isBlank(guest.getHoTenNV()) || isBlank(guest.getSdt()) || isBlank(guest.getIdNo())) {
                return new BookingConfirmationResult(false, null, "Mỗi khách cần đầy đủ họ tên, số điện thoại và CCCD/Hộ chiếu.");
            }
        }

        String bookingCode = "BK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return new BookingConfirmationResult(true, bookingCode, "Đặt phòng thành công.");
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
