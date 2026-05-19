package kqlhotel.bus.booking;

public class GuestInfoDto {

    private final String fullName;
    private final String phone;
    private final String idNo;
    private final String email;

    public GuestInfoDto(String fullName, String phone, String idNo) {
        this(fullName, phone, idNo, "");
    }

    public GuestInfoDto(String fullName, String phone, String idNo, String email) {
        this.fullName = fullName;
        this.phone = phone;
        this.idNo = idNo;
        this.email = email == null ? "" : email;
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

    public String getEmail() {
        return email;
    }

    // ===== (OPTIONAL - GIỮ LẠI CHO AN TOÀN) =====
    public String getHoTenNV() {
        return fullName;
    }

    public String getSdt() {
        return phone;
    }
}
