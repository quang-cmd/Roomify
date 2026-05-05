package kqlhotel.entity;

public class Phong {
    private String maPhong;
    private Double tienCoc;
    private LoaiPhong loaiPhong;
    private Integer tang;
    private String trangThaiPhong;

    public Phong() {
    }

    public Phong(String maPhong) {
        this.maPhong = maPhong;
    }

    public Phong(String maPhong, Double tienCoc, LoaiPhong loaiPhong, Integer tang, String trangThaiPhong) {
        this.maPhong = maPhong;
        this.tienCoc = tienCoc;
        this.loaiPhong = loaiPhong;
        this.tang = tang;
        this.trangThaiPhong = trangThaiPhong;
    }

    public String getMaPhong() { return maPhong; }
    public String getRoomId() { return maPhong; }
    public void setMaPhong(String maPhong) { this.maPhong = maPhong; }

    public Double getTienCoc() { return tienCoc; }
    public Double getDeposit() { return tienCoc; }
    public void setTienCoc(Double tienCoc) { this.tienCoc = tienCoc; }

    public LoaiPhong getLoaiPhong() { return loaiPhong; }
    public LoaiPhong getRoomType() { return loaiPhong; }
    public void setLoaiPhong(LoaiPhong loaiPhong) { this.loaiPhong = loaiPhong; }

    public Integer getTang() { return tang; }
    public Integer getFloor() { return tang; }
    public void setTang(Integer tang) { this.tang = tang; }

    public String getTrangThaiPhong() { return trangThaiPhong; }
    public String getStatus() { return trangThaiPhong; }
    public void setTrangThaiPhong(String trangThaiPhong) { this.trangThaiPhong = trangThaiPhong; }
}
