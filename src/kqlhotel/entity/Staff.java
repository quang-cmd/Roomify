package kqlhotel.entity;

public class Staff {
    private String maNV;
    private String hoTenNV;
    private String sdt;
    private boolean gioiTinh;
    private String taiKhoan;

    public Staff() {}

    // Getters and Setters
    public String getMaNV() { return maNV; }
    public void setMaNV(String maNV) { this.maNV = maNV; }
    public String getHoTenNV() { return hoTenNV; }
    public void setHoTenNV(String hoTenNV) { this.hoTenNV = hoTenNV; }
    public String getSdt() { return sdt; }
    public void setSdt(String sdt) { this.sdt = sdt; }
    public boolean isGioiTinh() { return gioiTinh; }
    public void setGioiTinh(boolean gioiTinh) { this.gioiTinh = gioiTinh; }
    public String getTaiKhoan() { return taiKhoan; }
    public void setTaiKhoan(String taiKhoan) { this.taiKhoan = taiKhoan; }
}
