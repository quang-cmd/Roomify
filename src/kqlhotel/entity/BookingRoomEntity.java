package kqlhotel.entity;

import java.time.LocalDateTime;

public class BookingRoomEntity {
    private String bookingId;
    private String roomId;
    private long nightlyRate;
    private LocalDateTime ngayNhanDuKien;
    private LocalDateTime ngayTraDuKien;
    private double donGiaDat;
    private int soLuongNguoiO;
    private String ghiChu;

    public String getBookingId() {
        return bookingId;
    }

    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
    }

    public String getMaDatPhong() {
        return bookingId;
    }

    public void setMaDatPhong(String maDatPhong) {
        this.bookingId = maDatPhong;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getMaPhong() {
        return roomId;
    }

    public void setMaPhong(String maPhong) {
        this.roomId = maPhong;
    }

    public long getNightlyRate() {
        return nightlyRate > 0 ? nightlyRate : Math.round(donGiaDat);
    }

    public void setNightlyRate(long nightlyRate) {
        this.nightlyRate = nightlyRate;
        this.donGiaDat = nightlyRate;
    }

    public LocalDateTime getNgayNhanDuKien() {
        return ngayNhanDuKien;
    }

    public void setNgayNhanDuKien(LocalDateTime ngayNhanDuKien) {
        this.ngayNhanDuKien = ngayNhanDuKien;
    }

    public LocalDateTime getNgayTraDuKien() {
        return ngayTraDuKien;
    }

    public void setNgayTraDuKien(LocalDateTime ngayTraDuKien) {
        this.ngayTraDuKien = ngayTraDuKien;
    }

    public double getDonGiaDat() {
        return donGiaDat;
    }

    public void setDonGiaDat(double donGiaDat) {
        this.donGiaDat = donGiaDat;
        this.nightlyRate = Math.round(donGiaDat);
    }

    public int getSoLuongNguoiO() {
        return soLuongNguoiO;
    }

    public void setSoLuongNguoiO(int soLuongNguoiO) {
        this.soLuongNguoiO = soLuongNguoiO;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }
}
