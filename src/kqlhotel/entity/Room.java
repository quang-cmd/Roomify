package kqlhotel.entity;

public class Room extends Phong {
    public Room() {
        super();
    }

    public Room(String maPhong) {
        super(maPhong);
    }

    public Room(String maPhong, Double tienCoc, LoaiPhong loaiPhong, Integer tang, String trangThaiPhong) {
        super(maPhong, tienCoc, loaiPhong, tang, trangThaiPhong);
    }
}
