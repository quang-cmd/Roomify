package kqlhotel.entity;

public class DoiPhongRoomOption {
    private String maPhong;
    private String tenLoaiPhong;
    private int tang;
    private String trangThaiPhong;

    public String getMaPhong() {
        return maPhong;
    }

    public void setMaPhong(String maPhong) {
        this.maPhong = maPhong;
    }

    public String getTenLoaiPhong() {
        return tenLoaiPhong;
    }

    public void setTenLoaiPhong(String tenLoaiPhong) {
        this.tenLoaiPhong = tenLoaiPhong;
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

    @Override
    public String toString() {
        return maPhong + " - " + tenLoaiPhong + " - T\u1ea7ng " + tang;
    }
}
