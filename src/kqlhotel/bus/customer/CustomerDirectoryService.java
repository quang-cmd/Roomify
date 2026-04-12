package kqlhotel.bus.customer;

import java.util.Optional;
import kqlhotel.bus.booking.model.GuestInfoDto;

public interface CustomerDirectoryService {
    Optional<GuestInfoDto> findByIdNo(String idNo);
}
