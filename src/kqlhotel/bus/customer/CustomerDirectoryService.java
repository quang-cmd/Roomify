package kqlhotel.bus.customer;

import java.util.Optional;
import kqlhotel.bus.booking.GuestInfoDto;

public interface CustomerDirectoryService {
    Optional<GuestInfoDto> findByIdNo(String idNo);
}
