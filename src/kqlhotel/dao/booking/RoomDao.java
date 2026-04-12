package kqlhotel.dao.booking;

import java.time.LocalDate;
import java.util.List;
import kqlhotel.entity.RoomEntity;

public interface RoomDao {
    List<RoomEntity> findAvailableRooms(String roomType, LocalDate checkInDate, LocalDate checkOutDate, int guests);
}
