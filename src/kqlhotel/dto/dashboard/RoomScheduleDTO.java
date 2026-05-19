package kqlhotel.dto.dashboard;

public class RoomScheduleDTO {
    private final String roomCode;
    private final String customerName;
    private final String time;
    private final String note;

    public RoomScheduleDTO(String roomCode, String customerName, String time, String note) {
        this.roomCode = roomCode;
        this.customerName = customerName;
        this.time = time;
        this.note = note;
    }

    public String getRoomCode() {
        return roomCode;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getTime() {
        return time;
    }

    public String getNote() {
        return note;
    }
}