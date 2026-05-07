package kqlhotel.gui;

public class Permission {
    private Permission() {}

    public static boolean isQuanLy() {
        return Session.currentAccount != null
                && "QuanLy".equalsIgnoreCase(Session.currentAccount.getRole());
    }

    public static boolean isNhanVien() {
        return Session.currentAccount != null
                && "NhanVien".equalsIgnoreCase(Session.currentAccount.getRole());
    }

    public static boolean canAccess(String route) {
        if (route == null || route.isBlank()) {
            return false;
        }

        // Quản lý được dùng tất cả trừ Đặt phòng
        if (isQuanLy()) {
            return !"booking".equals(route);
        }

        // Nhân viên được dùng các màn hình nghiệp vụ chính
        if (isNhanVien()) {
            return switch (route) {
                case "dashboard",
                     "booking",
                     "check-in",
                     "checkout",
                     "swap-room",
                     "cancel-room",
                     "room-management",
                     "invoices",
                     "statistics",
                     "customers" -> true;
                default -> false;
            };
        }

        return false;
    }

    public static String getDefaultRoute() {
        return "dashboard";
    }
}