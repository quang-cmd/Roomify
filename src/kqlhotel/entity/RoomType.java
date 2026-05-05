package kqlhotel.entity;

public class RoomType extends LoaiPhong {
    public RoomType() {
        super();
    }

    public RoomType(String maLoaiPhong, String tenLoaiPhong, int soLuongPhong, double giaPhong, int sucChuaToiDa, Double dienTich, String moTa, String tienNghi) {
        super(maLoaiPhong, tenLoaiPhong, soLuongPhong, giaPhong, sucChuaToiDa, dienTich, moTa, tienNghi);
    }
}
