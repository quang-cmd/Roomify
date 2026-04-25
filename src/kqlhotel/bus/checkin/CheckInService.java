package kqlhotel.bus.checkin;

import java.time.LocalDate;
import java.util.List;
import kqlhotel.bus.checkin.model.ArrivalDto;
import kqlhotel.bus.checkin.model.CheckInResult;

/**
 * Front-desk check-in workflow. Bridges DatPhong (reservation) with
 * ChiTietHoaDon (invoice line per room) so downstream services
 * (Checkout / Invoices / Statistics) have data to work with.
 */
public interface CheckInService {

    /**
     * Find bookings whose expected check-in date falls within [from, to]
     * (inclusive). Pass empty/null keyword to return all; otherwise filter
     * by maDatPhong / customer name / phone / CCCD.
     */
    List<ArrivalDto> findArrivals(LocalDate from, LocalDate to, String keyword);

    /**
     * Confirm physical arrival of guests for the given booking.
     * Side-effects (in one transaction):
     *   - INSERT ChiTietHoaDon for every ChiTietDatPhong row
     *   - UPDATE Phong.trangThaiPhong = 'DangSuDung' for those rooms
     */
    CheckInResult confirmCheckIn(String maDatPhong);
}
