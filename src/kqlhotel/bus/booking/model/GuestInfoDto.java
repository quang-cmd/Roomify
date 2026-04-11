package kqlhotel.bus.booking.model;

public class GuestInfoDto {
    private final String fullName;
    private final String phone;
    private final String idNo;

    public GuestInfoDto(String fullName, String phone, String idNo) {
        this.fullName = fullName;
        this.phone = phone;
        this.idNo = idNo;
    }

    public String getFullName() {
        return fullName;
    }

    public String getPhone() {
        return phone;
    }

    public String getIdNo() {
        return idNo;
    }
}
