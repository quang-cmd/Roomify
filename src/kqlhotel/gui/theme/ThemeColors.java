package kqlhotel.gui.theme;

import java.awt.Color;

/**
 * Calm Slate palette - tối ưu cho dashboard nội bộ, dùng lâu không mỏi mắt.
 * Tokens được tổ chức theo nhóm: surface / text / brand / status / sidebar.
 */
public final class ThemeColors {
    private ThemeColors() {
    }

    // ===== Surfaces =====
    public static final Color BG_PRIMARY      = new Color(0xF1F5F9); // slate-100, nền chính
    public static final Color BG_SECONDARY    = new Color(0xE2E8F0); // slate-200
    public static final Color SURFACE         = new Color(0xFFFFFF); // card / panel
    public static final Color SURFACE_LIGHT   = new Color(0xF8FAFC); // input / zebra row
    public static final Color SURFACE_HOVER   = new Color(0xEFF4FA); // hover row / button

    // ===== Borders =====
    public static final Color BORDER          = new Color(0xCBD5E1); // slate-300
    public static final Color BORDER_SOFT     = new Color(0xE2E8F0); // slate-200

    // ===== Text =====
    public static final Color TEXT_PRIMARY    = new Color(0x0F172A); // slate-900
    public static final Color TEXT_SECONDARY  = new Color(0x334155); // slate-700
    public static final Color TEXT_MUTED      = new Color(0x64748B); // slate-500
    public static final Color TEXT_PLACEHOLDER= new Color(0x94A3B8); // slate-400
    public static final Color TEXT_ON_DARK    = new Color(0xF8FAFC);

    // ===== Brand =====
    public static final Color PRIMARY         = new Color(0x2563EB); // indigo-blue 600
    public static final Color PRIMARY_DARK    = new Color(0x1D4ED8); // hover
    public static final Color PRIMARY_SOFT    = new Color(0xDBEAFE); // tag bg
    public static final Color ACCENT          = new Color(0xF59E0B); // amber-500 (CTA)
    public static final Color ACCENT_DARK     = new Color(0xD97706); // hover
    public static final Color ACCENT_SOFT     = new Color(0xFEF3C7);

    // ===== Semantic =====
    public static final Color SUCCESS         = new Color(0x16A34A);
    public static final Color SUCCESS_SOFT    = new Color(0xDCFCE7);
    public static final Color DANGER          = new Color(0xDC2626);
    public static final Color DANGER_SOFT     = new Color(0xFEE2E2);
    public static final Color WARNING         = new Color(0xF59E0B);
    public static final Color WARNING_SOFT    = new Color(0xFEF3C7);
    public static final Color INFO            = new Color(0x0EA5E9);
    public static final Color INFO_SOFT       = new Color(0xE0F2FE);

    // ===== Sidebar =====
    public static final Color SIDEBAR_BG      = new Color(0x1E293B); // slate-800
    public static final Color SIDEBAR_ITEM    = new Color(0x334155); // slate-700
    public static final Color SIDEBAR_ITEM_HOVER = new Color(0x475569); // slate-600
    public static final Color SIDEBAR_DIVIDER = new Color(0x0F172A);
    public static final Color SIDEBAR_TEXT    = new Color(0xCBD5E1);
    public static final Color SIDEBAR_TEXT_MUTED = new Color(0x94A3B8);

    // ===== Legacy aliases (giữ tương thích các panel cũ) =====
    public static final Color DEMO_BG         = PRIMARY_SOFT;
    public static final Color DEMO_BORDER     = new Color(0xBFDBFE);
    public static final Color DEMO_TEXT       = PRIMARY_DARK;

    // Brand
    public static final Color PREMIUM_PRIMARY         = new Color(0x1E3A8A); // blue-900 (deep navy)
    public static final Color PREMIUM_PRIMARY_DARK    = new Color(0x172E6F); // hover / pressed
    public static final Color PREMIUM_PRIMARY_SOFT    = new Color(0xDBEAFE); // soft tag bg
    public static final Color PREMIUM_ACCENT          = new Color(0x7C3AED); // violet-600 (CTA secondary)
    public static final Color PREMIUM_ACCENT_DARK     = new Color(0x6D28D9);
    public static final Color PREMIUM_ACCENT_SOFT     = new Color(0xEDE9FE);

    // Surfaces
    public static final Color PREMIUM_BG              = new Color(0xFAFAFA);
    public static final Color PREMIUM_SURFACE         = new Color(0xFFFFFF);
    public static final Color PREMIUM_SURFACE_HOVER   = new Color(0xF3F4F6); // gray-100

    // Borders
    public static final Color PREMIUM_BORDER          = new Color(0xE5E7EB); // gray-200
    public static final Color PREMIUM_BORDER_SOFT     = new Color(0xF3F4F6);

    // Text
    public static final Color PREMIUM_TEXT_PRIMARY    = new Color(0x111827); // gray-900
    public static final Color PREMIUM_TEXT_SECONDARY  = new Color(0x374151); // gray-700
    public static final Color PREMIUM_TEXT_MUTED      = new Color(0x6B7280); // gray-500

    // Light sidebar
    public static final Color PREMIUM_SIDEBAR_BG          = new Color(0xFFFFFF);
    public static final Color PREMIUM_SIDEBAR_BORDER      = new Color(0xE5E7EB);
    public static final Color PREMIUM_SIDEBAR_TEXT        = new Color(0x374151);
    public static final Color PREMIUM_SIDEBAR_TEXT_MUTED  = new Color(0x9CA3AF);
    public static final Color PREMIUM_SIDEBAR_HOVER       = new Color(0xF3F4F6);
    public static final Color PREMIUM_SIDEBAR_ACTIVE_BG   = PREMIUM_PRIMARY;
    public static final Color PREMIUM_SIDEBAR_ACTIVE_TEXT = new Color(0xFFFFFF);

    /**
     * Chuẩn hoá trạng thái phòng về SQL code (Trong / DangSuDung / BaoTri).
     * Chấp nhận cả mã SQL và nhãn hiển thị tiếng Việt.
     * Backward-compat: "DaDat" / "Đã đặt" / "DangDon" / "Đang dọn" được map về
     * "DangSuDung" để dữ liệu cũ vẫn render được.
     */
    public static String normalizeStatus(String status) {
        if (status == null) return null;
        String s = status.trim();
        switch (s) {
            case "Trong":
            case "Trống":
                return "Trong";
            case "DangSuDung":
            case "Đang sử dụng":
                return "DangSuDung";
            case "BaoTri":
            case "Bảo trì":
                return "BaoTri";
            default:
                return s;
        }
    }

    /**
     * Trả màu chữ chính cho trạng thái phòng (dùng đồng bộ toàn app).
     */
    public static Color statusColor(String status) {
        String code = normalizeStatus(status);
        if (code == null) return TEXT_MUTED;
        switch (code) {
            case "Trong":      return SUCCESS;
            case "DangSuDung": return new Color(239, 68, 68); // Red-500
            case "BaoTri":     return WARNING;
            default:           return TEXT_MUTED;
        }
    }

    /**
     * Trả màu nền nhạt tương ứng cho badge trạng thái phòng.
     */
    public static Color statusBackground(String status) {
        String code = normalizeStatus(status);
        if (code == null) return BG_SECONDARY;
        switch (code) {
            case "Trong":      return SUCCESS_SOFT;
            case "DangSuDung": return new Color(254, 226, 226); // Red-100
            case "BaoTri":     return WARNING_SOFT;
            default:           return BG_SECONDARY;
        }
    }

    /**
     * Nhãn tiếng Việt chuẩn cho trạng thái phòng.
     */
    public static String statusLabel(String status) {
        String code = normalizeStatus(status);
        if (code == null) return "";
        switch (code) {
            case "Trong":      return "Trống";
            case "DangSuDung": return "Đang sử dụng";
            case "BaoTri":     return "Bảo trì";
            default:           return code;
        }
    }

    /**
     * Tạo màu accent kèm độ trong suốt (alpha 0-255).
     */
    public static Color withAlpha(Color base, int alpha) {
        return new Color(base.getRed(), base.getGreen(), base.getBlue(), alpha);
    }
}
