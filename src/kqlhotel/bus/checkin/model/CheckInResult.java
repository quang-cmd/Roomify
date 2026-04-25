package kqlhotel.bus.checkin.model;

public class CheckInResult {
    private final boolean success;
    private final String maHD;
    private final int roomCount;
    private final String message;

    public CheckInResult(boolean success, String maHD, int roomCount, String message) {
        this.success = success;
        this.maHD = maHD;
        this.roomCount = roomCount;
        this.message = message;
    }

    public static CheckInResult ok(String maHD, int roomCount, String message) {
        return new CheckInResult(true, maHD, roomCount, message);
    }

    public static CheckInResult fail(String message) {
        return new CheckInResult(false, null, 0, message);
    }

    public boolean isSuccess() { return success; }
    public String getMaHD() { return maHD; }
    public int getRoomCount() { return roomCount; }
    public String getMessage() { return message; }
}
