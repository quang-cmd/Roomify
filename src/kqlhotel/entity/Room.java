package kqlhotel.entity;

public class Room {
    private String maPhong;
    private String loaiPhong;
    private int tang;
    private String trangThaiPhong;

    public Room() {
    }

    public Room(String maPhong) {
        this.maPhong = maPhong;
    }

    public String getMaPhong() {
        return maPhong;
    }

    public void setMaPhong(String maPhong) {
        this.maPhong = maPhong;
    }

    public String getLoaiPhong() {
        return loaiPhong;
    }

    public void setLoaiPhong(String loaiPhong) {
        this.loaiPhong = loaiPhong;
    }

    public int getTang() {
        return tang;
    }

    public void setTang(int tang) {
        this.tang = tang;
    }

    public String getTrangThaiPhong() {
        return trangThaiPhong;
    }

    public void setTrangThaiPhong(String trangThaiPhong) {
        this.trangThaiPhong = trangThaiPhong;
    }
}
