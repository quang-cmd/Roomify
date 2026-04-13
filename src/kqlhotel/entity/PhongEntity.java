package kqlhotel.entity;

public class PhongEntity {
    private String maPhong;
    private String trangThai;

    public PhongEntity() {}

    public PhongEntity(String maPhong, String trangThai) {
        this.maPhong = maPhong;
        this.trangThai = trangThai;
    }

    public String getMaPhong() {
        return maPhong;
    }

    public void setMaPhong(String maPhong) {
        this.maPhong = maPhong;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }
}