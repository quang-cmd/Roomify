package kqlhotel.bus.customer;

import java.util.Optional;
import kqlhotel.bus.booking.GuestInfoDto;

public class PendingCustomerDirectoryService implements CustomerDirectoryService {
    @Override
    public Optional<GuestInfoDto> findByIdNo(String idNo) {
        return Optional.empty();
    }
}
