package kqlhotel.dao.booking;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import kqlhotel.entity.BookingEntity;
import kqlhotel.entity.BookingRoomEntity;
import kqlhotel.entity.Customer;
import kqlhotel.entity.GuestStayDetail;
import kqlhotel.entity.Invoice;

public interface BookingDao {
    String createBooking(BookingEntity booking);

    void createBookingRooms(List<BookingRoomEntity> bookingRooms);

    String createBooking(Connection con, BookingEntity booking) throws SQLException;

    void createBookingRooms(Connection con, List<BookingRoomEntity> bookingRooms) throws SQLException;

    void createGuestStayDetails(Connection con, List<GuestStayDetail> guestStayDetails) throws SQLException;

    String createInvoice(Connection con, Invoice invoice) throws SQLException;

    String pickAvailableRoomId(Connection con, String roomTypeName, LocalDate checkIn,
                               LocalDate checkOut, Set<String> excludeRoomIds) throws SQLException;

    String upsertCustomer(Connection con, Customer customer) throws SQLException;

    String findCustomerNameByPhoneWithDifferentId(Connection con, String phone, String idNo) throws SQLException;

    String resolveStaffId(Connection con) throws SQLException;
}
