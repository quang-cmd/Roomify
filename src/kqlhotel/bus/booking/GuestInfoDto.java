package kqlhotel.bus.booking;

public class GuestInfoDto {

    private final String fullName;
    private final String phone;
    private final String idNo;

    public GuestInfoDto(String fullName, String phone, String idNo) {
        this.fullName = fullName;
        this.phone = phone;
        this.idNo = idNo;
    }

    // ===== GETTER CHUẨN GUI =====
    public String getFullName() {
        return fullName;
    }

    public String getPhone() {
        return phone;
    }

    public String getIdNo() {
        return idNo;
    }

    // ===== (OPTIONAL - GIỮ LẠI CHO AN TOÀN) =====
    public String getHoTenNV() {
        return fullName;
    }

    public String getSdt() {
        return phone;
    }
}
