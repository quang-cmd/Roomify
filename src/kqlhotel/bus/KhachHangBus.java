package kqlhotel.bus;

import java.util.List;
import java.util.stream.Collectors;
import kqlhotel.dao.KhachHangDao;
import kqlhotel.entity.Customer;

public class KhachHangBus {
    private final KhachHangDao khachHangDao = new KhachHangDao();

    public List<Customer> getAllCustomers() {
        return khachHangDao.getAll();
    }

    public Customer getCustomerById(String maKH) {
        if (isBlank(maKH)) {
            return null;
        }
        return khachHangDao.getById(maKH.trim());
    }

    public List<Customer> filterCustomers(String keyword, String rank) {
        String normalizedKeyword = safeTrim(keyword);
        String normalizedRank = safeTrim(rank);

        if (normalizedKeyword.isEmpty() && normalizedRank.isEmpty()) {
            return getAllCustomers();
        }
        if (!normalizedKeyword.isEmpty() && normalizedRank.isEmpty()) {
            return khachHangDao.search(normalizedKeyword);
        }
        if (normalizedKeyword.isEmpty()) {
            return khachHangDao.filterByRank(normalizedRank);
        }

        return khachHangDao.search(normalizedKeyword).stream()
            .filter(customer -> normalizedRank.equalsIgnoreCase(safeTrim(customer.getHangKH())))
            .collect(Collectors.toList());
    }

    public boolean createCustomer(Customer customer) {
        if (!isValid(customer, true)) {
            return false;
        }
        normalizeCustomer(customer);
        if (khachHangDao.getById(customer.getMaKH()) != null) {
            return false;
        }
        return khachHangDao.create(customer);
    }

    public boolean updateCustomer(Customer customer) {
        if (!isValid(customer, true)) {
            return false;
        }
        normalizeCustomer(customer);
        if (khachHangDao.getById(customer.getMaKH()) == null) {
            return false;
        }
        return khachHangDao.update(customer);
    }

    public boolean deleteCustomer(String maKH) {
        if (isBlank(maKH)) {
            return false;
        }
        return khachHangDao.delete(maKH.trim());
    }

    public boolean isValid(Customer customer, boolean requireId) {
        if (customer == null) {
            return false;
        }
        if (requireId && isBlank(customer.getMaKH())) {
            return false;
        }
        if (isBlank(customer.getHoTenKH()) || isBlank(customer.getSdt())) {
            return false;
        }
        return !isBlank(customer.getHangKH());
    }

    private void normalizeCustomer(Customer customer) {
        customer.setMaKH(safeTrim(customer.getMaKH()));
        customer.setHoTenKH(safeTrim(customer.getHoTenKH()));
        customer.setEmail(safeTrim(customer.getEmail()));
        customer.setSdt(safeTrim(customer.getSdt()));
        customer.setCCCD(safeTrim(customer.getCCCD()));
        customer.setQuocTich(safeTrim(customer.getQuocTich()));
        customer.setDiaChi(safeTrim(customer.getDiaChi()));
        customer.setHangKH(safeTrim(customer.getHangKH()));
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }
}
