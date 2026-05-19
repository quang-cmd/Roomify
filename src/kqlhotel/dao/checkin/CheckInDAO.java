package kqlhotel.dao.checkin;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import kqlhotel.bus.checkin.model.ArrivalDto;
import kqlhotel.bus.checkin.model.RoomCheckInCommand;

public interface CheckInDAO {
    List<ArrivalDto> findArrivals(LocalDate from, LocalDate to, String keyword);
    String getInvoiceIdByBooking(String maDatPhong) throws Exception;
    boolean hasInvoiceDetails(String maHD) throws Exception;
    boolean syncExistingCheckIn(String maDatPhong, String maHD) throws Exception;
    List<Object[]> getReservedRooms(String maDatPhong) throws Exception;
    String getInvoiceStatus(String maHD) throws Exception;
    void executeCheckInTransaction(String maDatPhong, String maHD, List<RoomCheckInCommand> rooms, BigDecimal totalRoom) throws Exception;
}
