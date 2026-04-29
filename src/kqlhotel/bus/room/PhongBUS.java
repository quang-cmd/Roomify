package kqlhotel.bus.room;

import kqlhotel.dao.room.PhongDAO;
import kqlhotel.entity.Phong;

import java.util.List;

public class PhongBUS {
    private PhongDAO phongDAO;

    public PhongBUS() {
        phongDAO = new PhongDAO();
    }

    public List<Phong> getAllRooms() {
        return phongDAO.getAll();
    }

    public List<Phong> searchRooms(String maPhong, String tenLoaiPhong, String trangThaiGUI) {
        // Map GUI status text to DB text
        String trangThaiDB = mapGuiStatusToDbStatus(trangThaiGUI);
        return phongDAO.search(maPhong, tenLoaiPhong, trangThaiDB);
    }
    
    public String mapGuiStatusToDbStatus(String guiStatus) {
        if (guiStatus == null) return null;
        switch (guiStatus) {
            case "Trống": return "Trong";
            case "Đang sử dụng": return "DangSuDung";
            case "Bảo trì": return "BaoTri";
            default: return guiStatus; // e.g. "Tất cả trạng thái"
        }
    }

    public String mapDbStatusToGuiStatus(String dbStatus) {
        if (dbStatus == null) return "Không xác định";
        switch (dbStatus) {
            case "Trong":      return "Trống";
            case "DangSuDung": return "Đang sử dụng";
            case "BaoTri":     return "Bảo trì";
            default: return dbStatus;
        }
    }

    public long countByStatus(List<Phong> list, String dbStatus) {
        if (list == null) return 0;
        return list.stream().filter(p -> dbStatus.equals(p.getTrangThaiPhong())).count();
    }
    public boolean addRoom(Phong p) {
        if (p.getMaPhong() == null || p.getMaPhong().trim().isEmpty()) return false;
        if (p.getTrangThaiPhong() == null) p.setTrangThaiPhong("Trong");
        return phongDAO.create(p);
    }

    public boolean updateRoom(Phong p) {
        if (p.getMaPhong() == null || p.getMaPhong().trim().isEmpty()) return false;
        return phongDAO.update(p);
    }
}
