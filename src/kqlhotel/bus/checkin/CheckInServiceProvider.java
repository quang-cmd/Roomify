package kqlhotel.bus.checkin;

public final class CheckInServiceProvider {
    private static volatile CheckInService instance;

    private CheckInServiceProvider() {}

    public static CheckInService getInstance() {
        CheckInService local = instance;
        if (local == null) {
            synchronized (CheckInServiceProvider.class) {
                local = instance;
                if (local == null) {
                    local = new SqlCheckInService();
                    instance = local;
                }
            }
        }
        return local;
    }

    public static void overrideInstance(CheckInService service) {
        instance = service;
    }
}
