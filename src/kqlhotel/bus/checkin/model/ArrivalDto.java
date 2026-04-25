package kqlhotel.bus.checkin.model;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Snapshot of a booking ready to be checked in. Aggregates booking + customer
 * + allocated room codes for the front-desk arrival list.
 */
public class ArrivalDto {
    private final String maDatPhong;
    private final String maHD;             // null if invoice not yet created (defensive)
    private final LocalDateTime ngayDat;
    private final LocalDateTime ngayNhanDuKien;
    private final LocalDateTime ngayTraDuKien;
    private final long tienCoc;
    private final String tenKH;
    private final String sdtKH;
    private final String cccdKH;
    private final List<String> roomCodes;
    private final int nights;
    private final boolean checkedIn;        // true if ChiTietHoaDon already exists for this booking

    public ArrivalDto(String maDatPhong, String maHD, LocalDateTime ngayDat,
                      LocalDateTime ngayNhanDuKien, LocalDateTime ngayTraDuKien,
                      long tienCoc, String tenKH, String sdtKH, String cccdKH,
                      List<String> roomCodes, int nights, boolean checkedIn) {
        this.maDatPhong = maDatPhong;
        this.maHD = maHD;
        this.ngayDat = ngayDat;
        this.ngayNhanDuKien = ngayNhanDuKien;
        this.ngayTraDuKien = ngayTraDuKien;
        this.tienCoc = tienCoc;
        this.tenKH = tenKH;
        this.sdtKH = sdtKH;
        this.cccdKH = cccdKH;
        this.roomCodes = roomCodes;
        this.nights = nights;
        this.checkedIn = checkedIn;
    }

    public String getMaDatPhong() { return maDatPhong; }
    public String getMaHD() { return maHD; }
    public LocalDateTime getNgayDat() { return ngayDat; }
    public LocalDateTime getNgayNhanDuKien() { return ngayNhanDuKien; }
    public LocalDateTime getNgayTraDuKien() { return ngayTraDuKien; }
    public long getTienCoc() { return tienCoc; }
    public String getTenKH() { return tenKH; }
    public String getSdtKH() { return sdtKH; }
    public String getCccdKH() { return cccdKH; }
    public List<String> getRoomCodes() { return roomCodes; }
    public int getNights() { return nights; }
    public boolean isCheckedIn() { return checkedIn; }
    public int getRoomCount() { return roomCodes == null ? 0 : roomCodes.size(); }
}
