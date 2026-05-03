package kqlhotel.bus.swaproom;

import java.util.List;
import kqlhotel.dao.swaproom.SwapRoomDAO;
import kqlhotel.entity.SwapRoomOption;
import kqlhotel.entity.SwapRoomSearchResult;

public class SwapRoomBUS {

    private final SwapRoomDAO dao;

    public SwapRoomBUS() {
        dao = new SwapRoomDAO();
    }

    public List<SwapRoomSearchResult> searchBookings(String bookingId, String guestName, String phoneNumber, String roomId) {
        return dao.searchBookings(bookingId, guestName, phoneNumber, roomId);
    }

    public List<SwapRoomOption> getAvailableRooms(SwapRoomSearchResult booking) {
        return dao.getAvailableRooms(booking);
    }

    public boolean changeRoom(String maDatPhong, String oldRoom, String newRoom) {
        return dao.changeRoom(maDatPhong, oldRoom, newRoom);
    }
}
