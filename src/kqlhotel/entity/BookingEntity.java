package kqlhotel.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class BookingEntity {
    private String bookingId;
    private LocalDateTime ngayDat;
    private double tienCoc;
    private String trangThaiDatPhong;
    private String ghiChu;
    private String maKH;
    private String maNV;
    private String customerName;
    private String customerPhone;
    private String customerIdNo;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private long totalAmount;
    private String status;

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

    public LocalDateTime getNgayDat() {
        return ngayDat;
    }

    public void setNgayDat(LocalDateTime ngayDat) {
        this.ngayDat = ngayDat;
    }

    public double getTienCoc() {
        return tienCoc;
    }

    public void setTienCoc(double tienCoc) {
        this.tienCoc = tienCoc;
    }

    public String getTrangThaiDatPhong() {
        return trangThaiDatPhong != null ? trangThaiDatPhong : status;
    }

    public void setTrangThaiDatPhong(String trangThaiDatPhong) {
        this.trangThaiDatPhong = trangThaiDatPhong;
        this.status = trangThaiDatPhong;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    public String getMaKH() {
        return maKH;
    }

    public void setMaKH(String maKH) {
        this.maKH = maKH;
    }

    public String getMaNV() {
        return maNV;
    }

    public void setMaNV(String maNV) {
        this.maNV = maNV;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public String getCustomerIdNo() {
        return customerIdNo;
    }

    public void setCustomerIdNo(String customerIdNo) {
        this.customerIdNo = customerIdNo;
    }

    public LocalDate getCheckInDate() {
        return checkInDate;
    }

    public void setCheckInDate(LocalDate checkInDate) {
        this.checkInDate = checkInDate;
    }

    public LocalDate getCheckOutDate() {
        return checkOutDate;
    }

    public void setCheckOutDate(LocalDate checkOutDate) {
        this.checkOutDate = checkOutDate;
    }

    public long getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(long totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
        this.trangThaiDatPhong = status;
    }
}
