package kqlhotel.bus.promotion;

import kqlhotel.dao.promotion.PromotionDAO;
import kqlhotel.entity.Promotion;

import java.util.List;

public class PromotionsBUS {
    private final PromotionDAO promotionDAO = new PromotionDAO();

    public List<Promotion> getAllPromotions() {
        return promotionDAO.getAll();
    }

    public List<Promotion> filterPromotions(String status) {
        if (status == null || status.isEmpty() || status.equals("Tất cả")) {
            return getAllPromotions();
        }

        String dbStatus = switch (status) {
            case "Đang áp dụng" -> "DangHoatDong";
            case "Sắp diễn ra" -> "SapDienRa";
            case "Đã hết hạn" -> "HetHan";
            default -> "";
        };

        if (dbStatus.isBlank()) {
            return getAllPromotions();
        }

        return promotionDAO.searchByStatus(dbStatus);
    }

    public int[] getPromotionsCount() {
        int[] counts = new int[4];

        List<Promotion> all = getAllPromotions();
        counts[0] = all.size();

        for (Promotion km : all) {
            if ("DangHoatDong".equals(km.getTrangThaiKM())) {
                counts[1]++;
            } else if ("SapDienRa".equals(km.getTrangThaiKM())) {
                counts[2]++;
            } else if ("HetHan".equals(km.getTrangThaiKM())) {
                counts[3]++;
            }
        }

        return counts;
    }

    public boolean createPromotion(Promotion km) {
        if (km == null) {
            return false;
        }

        if (km.getDieuKienApDung() < 0
                || km.getTienKhuyenMai() < 0
                || km.getGiaTriToiDa() < 0) {
            return false;
        }

        return promotionDAO.create(km);
    }

    public boolean updatePromotion(Promotion km) {
        if (km == null) {
            return false;
        }

        if (km.getDieuKienApDung() < 0
                || km.getTienKhuyenMai() < 0
                || km.getGiaTriToiDa() < 0) {
            return false;
        }

        return promotionDAO.update(km);
    }

    public Promotion getPromotionById(String maKM) {
        if (maKM == null || maKM.isBlank()) {
            return null;
        }

        return promotionDAO.getById(maKM.trim());
    }
}