package kqlhotel.entity;

import java.time.LocalDateTime;

public class Payment {
    private String maTT;
    private LocalDateTime ngayTT;
    private double soTienTT;
    private String ghiChu;
    private String phuongThucTT;
    private String trangThaiTT;
    private String maHD;
    private String maPC;
    private String maNV;

    public Payment() {}

    public Payment(String maTT, LocalDateTime ngayTT, double soTienTT, String ghiChu,
                   String phuongThucTT, String trangThaiTT, String maHD, String maPC, String maNV) {
        this.maTT = maTT;
        this.ngayTT = ngayTT;
        this.soTienTT = soTienTT;
        this.ghiChu = ghiChu;
        this.phuongThucTT = phuongThucTT;
        this.trangThaiTT = trangThaiTT;
        this.maHD = maHD;
        this.maPC = maPC;
        this.maNV = maNV;
    }

    public String getMaTT() { return maTT; }
    public void setMaTT(String maTT) { this.maTT = maTT; }

    public LocalDateTime getNgayTT() { return ngayTT; }
    public void setNgayTT(LocalDateTime ngayTT) { this.ngayTT = ngayTT; }

    public double getSoTienTT() { return soTienTT; }
    public void setSoTienTT(double soTienTT) { this.soTienTT = soTienTT; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }

    public String getPhuongThucTT() { return phuongThucTT; }
    public void setPhuongThucTT(String phuongThucTT) { this.phuongThucTT = phuongThucTT; }

    public String getTrangThaiTT() { return trangThaiTT; }
    public void setTrangThaiTT(String trangThaiTT) { this.trangThaiTT = trangThaiTT; }

    public String getMaHD() { return maHD; }
    public void setMaHD(String maHD) { this.maHD = maHD; }

    public String getMaPC() { return maPC; }
    public void setMaPC(String maPC) { this.maPC = maPC; }

    public String getMaNV() { return maNV; }
    public void setMaNV(String maNV) { this.maNV = maNV; }
}
