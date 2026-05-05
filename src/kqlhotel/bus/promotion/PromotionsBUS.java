package kqlhotel.bus.promotion;

import kqlhotel.dao.ConnectDB;
import kqlhotel.dao.promotion.*;
import kqlhotel.entity.*;
import java.util.List;

public class PromotionsBUS {
    private PromotionDAO promotionDAO = new PromotionDAO();

    public List<Promotion> getAllPromotions() {
        return promotionDAO.getAll();
    }

    public List<Promotion> filterPromotions(String status) {
        if (status == null || status.isEmpty() || status.equals("Tất cả")) {
            return getAllPromotions();
        }
        
        // Map GUI text to database status
        String dbStatus = "";
        switch (status) {
            case "Đang áp dụng":
                dbStatus = "DangHoatDong";
                break;
            case "Sắp diễn ra":
                dbStatus = "SapDienRa";
                break;
            case "Đã hết hạn":
                dbStatus = "HetHan";
                break;
        }
        
        return promotionDAO.searchByStatus(dbStatus);
    }
    
    public int[] getPromotionsCount() {
        // Trả về [Tổng số, Đang hoạt động, Sắp diễn ra, Hết hạn]
        int[] counts = new int[4];
        List<Promotion> all = getAllPromotions();
        counts[0] = all.size();
        for (Promotion km : all) {
            if ("DangHoatDong".equals(km.getTrangThaiKM())) counts[1]++;
            else if ("SapDienRa".equals(km.getTrangThaiKM())) counts[2]++;
            else if ("HetHan".equals(km.getTrangThaiKM())) counts[3]++;
        }
        return counts;
    }

    public boolean createPromotion(Promotion km) {
        return promotionDAO.create(km);
    }

    public boolean updatePromotion(Promotion km) {
        return promotionDAO.update(km);
    }
    
    public Promotion getPromotionById(String maKM) {
        return promotionDAO.getById(maKM);
    }
}
