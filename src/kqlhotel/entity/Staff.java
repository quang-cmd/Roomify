package kqlhotel.entity;

public class Staff {

    private String maNV;
    private String hoTenNV;
    private String sdt;
    private String tenDangNhap;

    public Staff(String maNV, String hoTenNV, String sdt, String tenDangNhap) {
        this.maNV = maNV;
        this.hoTenNV = hoTenNV;
        this.sdt = sdt;
        this.tenDangNhap = tenDangNhap;
    }

    public String getMaNV() { return maNV; }
    public String getHoTenNV() { return hoTenNV; }
    public String getSdt() { return sdt; }
    public String getTenDangNhap() { return tenDangNhap; }

    // 🔥 THÊM CÁC HÀM NÀY
    public String getFullName() {
        return hoTenNV;
    }

    public String getPhone() {
        return sdt;
    }

    public String getAccount() {
        return tenDangNhap;
    }
}