package kqlhotel.entity;

public class GuestStayDetail {
    private String maDatPhong;
    private String maPhong;
    private String hoTen;
    private String cccd;
    private String sdt;
    private String vaiTro;

    public GuestStayDetail() {
    }

    public GuestStayDetail(String maDatPhong, String maPhong, String hoTen, String cccd, String sdt, String vaiTro) {
        this.maDatPhong = maDatPhong;
        this.maPhong = maPhong;
        this.hoTen = hoTen;
        this.cccd = cccd;
        this.sdt = sdt;
        this.vaiTro = vaiTro;
    }

    public String getMaDatPhong() { return maDatPhong; }
    public void setMaDatPhong(String maDatPhong) { this.maDatPhong = maDatPhong; }

    public String getMaPhong() { return maPhong; }
    public void setMaPhong(String maPhong) { this.maPhong = maPhong; }

    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }

    public String getCccd() { return cccd; }
    public void setCccd(String cccd) { this.cccd = cccd; }

    public String getSdt() { return sdt; }
    public void setSdt(String sdt) { this.sdt = sdt; }

    public String getVaiTro() { return vaiTro; }
    public void setVaiTro(String vaiTro) { this.vaiTro = vaiTro; }
}
