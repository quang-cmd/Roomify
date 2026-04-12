package kqlhotel.bus.customer;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import kqlhotel.bus.booking.model.GuestInfoDto;

public class MockCustomerDirectoryService implements CustomerDirectoryService {
    private final List<GuestInfoDto> mockCustomers = Arrays.asList(
        new GuestInfoDto("Nguyen Van A", "0901000001", "079123456789"),
        new GuestInfoDto("Tran Thi B", "0901000002", "079987654321"),
        new GuestInfoDto("Le Minh C", "0901000003", "079456789123")
    );

    @Override
    public Optional<GuestInfoDto> findByIdNo(String idNo) {
        String normalizedId = normalize(idNo);

        if (normalizedId.isEmpty()) {
            return Optional.empty();
        }

        for (GuestInfoDto customer : mockCustomers) {
            boolean idMatched = !normalizedId.isEmpty() && normalize(customer.getIdNo()).equals(normalizedId);
            if (idMatched) {
                return Optional.of(customer);
            }
        }

        return Optional.empty();
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}
