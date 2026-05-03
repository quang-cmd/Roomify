package kqlhotel.bus.customer;

import java.util.Optional;
import kqlhotel.bus.booking.GuestInfoDto;
import kqlhotel.dao.customer.CustomerDAO;
import kqlhotel.entity.Customer;

public class SqlCustomerDirectoryService implements CustomerDirectoryService {
    private final CustomerDAO customerDAO = new CustomerDAO();

    @Override
    public Optional<GuestInfoDto> findByIdNo(String idNo) {
        if (idNo == null || idNo.trim().isEmpty()) {
            return Optional.empty();
        }

        String normalizedIdNo = idNo.trim();

        for (Customer customer : customerDAO.getAll()) {
            if (normalizedIdNo.equals(customer.getCCCD())) {
                return Optional.of(new GuestInfoDto(
                        customer.getHoTenKH(),
                        customer.getSdt(),
                        customer.getCCCD()
                ));
            }
        }

        return Optional.empty();
    }
}
