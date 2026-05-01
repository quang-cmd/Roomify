package kqlhotel.bus.customer;

public final class CustomerDirectoryServiceProvider {
    private static CustomerDirectoryService service = new SqlCustomerDirectoryService();

    private CustomerDirectoryServiceProvider() {
    }

    public static CustomerDirectoryService get() {
        return service;
    }

    public static void set(CustomerDirectoryService customerDirectoryService) {
        if (customerDirectoryService == null) {
            throw new IllegalArgumentException("customerDirectoryService must not be null");
        }
        service = customerDirectoryService;
    }
}
