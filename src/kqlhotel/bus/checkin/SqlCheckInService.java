package kqlhotel.bus.checkin;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import kqlhotel.bus.checkin.model.ArrivalDto;
import kqlhotel.bus.checkin.model.CheckInResult;
import kqlhotel.bus.checkin.model.RoomCheckInCommand;
import kqlhotel.dao.checkin.CheckInDAO;
import kqlhotel.dao.checkin.SqlCheckInDAO;

public class SqlCheckInService implements CheckInService {
    private static final LocalTime EARLY_CHECKIN_START = LocalTime.of(5, 0);
    private static final LocalTime EARLY_CHECKIN_50 = LocalTime.of(9, 0);
    private static final LocalTime CHECKIN_STANDARD = LocalTime.of(14, 0);
    
    private final CheckInDAO checkInDAO;
    
    public SqlCheckInService() {
        this(new SqlCheckInDAO());
    }
    
    public SqlCheckInService(CheckInDAO checkInDAO) {
        this.checkInDAO = checkInDAO;
    }

    @Override
    public List<ArrivalDto> findArrivals(LocalDate from, LocalDate to, String keyword) {
        return checkInDAO.findArrivals(from, to, keyword);
    }

    @Override
    public CheckInResult confirmCheckIn(String maDatPhong) {
        if (isBlank(maDatPhong)) {
            return CheckInResult.fail("Thiếu mã đặt phòng.");
        }

        try {
            String maHD = checkInDAO.getInvoiceIdByBooking(maDatPhong);
            if (maHD == null) {
                return CheckInResult.fail("Chưa có hóa đơn cho booking này.");
            }

            // Validate trạng thái hóa đơn
            String trangThai = checkInDAO.getInvoiceStatus(maHD);
            if ("DaHuy".equals(trangThai)) {
                return CheckInResult.fail("Booking này đã bị hủy (Hóa đơn: " + maHD + "). Không thể nhận phòng.");
            }
            if (trangThai == null) {
                return CheckInResult.fail("Không tìm thấy hóa đơn hợp lệ cho booking này.");
            }

            if (checkInDAO.hasInvoiceDetails(maHD)) {
                boolean synced = checkInDAO.syncExistingCheckIn(maDatPhong, maHD);
                return CheckInResult.ok(
                        maHD,
                        0,
                        synced
                                ? "Booking da co chi tiet hoa don. Da dong bo lai trang thai nhan phong."
                                : "Booking nay da nhan phong truoc do."
                );
            }

            List<Object[]> reservedRooms = checkInDAO.getReservedRooms(maDatPhong);
            if (reservedRooms.isEmpty()) {
                return CheckInResult.fail("Booking không có phòng nào.");
            }

            LocalDateTime now = LocalDateTime.now();

            // Validate: không được nhận phòng trước ngày dự kiến
            LocalDate earliest = reservedRooms.stream()
                    .map(r -> ((Timestamp) r[1]).toLocalDateTime().toLocalDate())
                    .min(LocalDate::compareTo)
                    .orElse(LocalDate.now());
            if (LocalDate.now().isBefore(earliest)) {
                return CheckInResult.fail(
                    "Chưa đến ngày nhận phòng. Ngày nhận phòng dự kiến: "
                    + earliest.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")) + ".");
            }

            // Validate: phòng phải ở trạng thái trống
            for (Object[] row : reservedRooms) {
                String maPhong = (String) row[0];
                String trangThaiPhong = row.length > 4 ? (String) row[4] : "Trong"; // Fallback in case of mock/old data
                if (!"Trong".equals(trangThaiPhong)) {
                    return CheckInResult.fail("Phòng " + maPhong + " hiện không trống (Trạng thái: " + trangThaiPhong + "). Vui lòng đợi khách cũ trả phòng hoặc đổi phòng.");
                }
            }
            
            BigDecimal totalRoom = BigDecimal.ZERO;
            List<RoomCheckInCommand> commands = new ArrayList<>();
            
            for (Object[] row : reservedRooms) {
                String maPhong = (String) row[0];
                LocalDateTime ngayNhanDuKien = ((Timestamp) row[1]).toLocalDateTime();
                LocalDateTime ngayTraDuKien = ((Timestamp) row[2]).toLocalDateTime();
                BigDecimal donGia = (BigDecimal) row[3];

                LocalDateTime ngayNhanThucTe = now;

                if (!ngayTraDuKien.isAfter(ngayNhanThucTe)) {
                    ngayNhanThucTe = ngayNhanDuKien;
                }

                int soDem = (int) Math.max(1,
                        ChronoUnit.DAYS.between(
                                ngayNhanDuKien.toLocalDate(),
                                ngayTraDuKien.toLocalDate()
                        ));

                BigDecimal phuThu = calculateEarlyCheckInFee(donGia, ngayNhanThucTe);
                BigDecimal thanhTien = donGia.multiply(BigDecimal.valueOf(soDem)).add(phuThu);
                totalRoom = totalRoom.add(thanhTien);
                
                commands.add(new RoomCheckInCommand(maPhong, ngayNhanDuKien, ngayTraDuKien, ngayNhanThucTe, donGia, soDem, phuThu, thanhTien));
            }
            
            checkInDAO.executeCheckInTransaction(maDatPhong, maHD, commands, totalRoom);

            return CheckInResult.ok(
                    maHD,
                    commands.size(),
                    "Nhận phòng thành công. " + commands.size() + " phòng đã được kích hoạt."
            );

        } catch (Exception e) {
            e.printStackTrace();
            return CheckInResult.fail("Lỗi CSDL: " + e.getMessage());
        }
    }

    private BigDecimal calculateEarlyCheckInFee(BigDecimal nightlyRate, LocalDateTime checkInTime) {
        if (nightlyRate == null || checkInTime == null) {
            return BigDecimal.ZERO;
        }
        LocalTime time = checkInTime.toLocalTime();
        if (!time.isBefore(CHECKIN_STANDARD)) {
            return BigDecimal.ZERO;
        }
        if (time.isBefore(EARLY_CHECKIN_START)) {
            return nightlyRate;
        }
        if (!time.isBefore(EARLY_CHECKIN_50)) {
            return nightlyRate.multiply(BigDecimal.valueOf(0.3));
        }
        return nightlyRate.multiply(BigDecimal.valueOf(0.5));
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
