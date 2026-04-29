package kqlhotel.entity;

public class DoiPhongRoomOption {
    private String maPhong;
    private String maLoaiPhong;
    private String tenLoaiPhong;
    private int tang;
    private int sucChuaToiDa;
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

    public String getMaLoaiPhong() {
        return maLoaiPhong;
    }

    public void setMaLoaiPhong(String maLoaiPhong) {
        this.maLoaiPhong = maLoaiPhong;
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

    public int getSucChuaToiDa() {
        return sucChuaToiDa;
    }

    public void setSucChuaToiDa(int sucChuaToiDa) {
        this.sucChuaToiDa = sucChuaToiDa;
    }

    public String getTrangThaiPhong() {
        return trangThaiPhong;
    }

    public void setTrangThaiPhong(String trangThaiPhong) {
        this.trangThaiPhong = trangThaiPhong;
    }

    @Override
    public String toString() {
        return maPhong + " - " + tenLoaiPhong + " - T\u1ea7ng " + tang + " - " + sucChuaToiDa + " kh\u00e1ch";
    }
}
