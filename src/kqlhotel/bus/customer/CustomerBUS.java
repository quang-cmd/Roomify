package kqlhotel.bus.customer;

import java.util.List;
import kqlhotel.dao.customer.CustomerDAO;
import kqlhotel.entity.CustomerBookingHistory;
import kqlhotel.entity.Customer;

public class CustomerBUS {

    private final CustomerDAO dao = new CustomerDAO();

    public List<Customer> getAll() {
        return dao.getAll();
    }

    public List<Customer> getAllWithStats() {
        return dao.getAllWithStats();
    }

    public List<CustomerBookingHistory> getBookingHistory(String maKH) {
        return dao.getBookingHistory(maKH);
    }

    public boolean insert(Customer kh) {
        return dao.create(kh);
    }

    public boolean update(Customer kh) {
        return dao.update(kh);
    }
}
