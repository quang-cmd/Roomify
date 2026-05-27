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

        // Quan ly tap trung vao nhom quan tri, khong thao tac nghiep vu le tan.
        if (isQuanLy()) {
            return switch (route) {
                case "dashboard",
                     "room-management",
                     "staff",
                     "customers",
                     "services",
                     "promotions",
                     "invoices",
                     "statistics",
                     "help" -> true;
                default -> false;
            };
        }

        // Nhan vien le tan duoc dung cac man hinh nghiep vu, tru phan nhan su.
        // Quyen them phong/them loai phong duoc chan rieng trong RoomManagementPanel.
        if (isNhanVien()) {
            return switch (route) {
                case "dashboard",
                     "booking",
                     "check-in",
                     "checkout",
                     "swap-room",
                     "cancel-room",
                     "room-management",
                     "customers",
                     "services",
                     "promotions",
                     "invoices",
                     "statistics",
                     "help" -> true;
                default -> false;
            };
        }

        return false;
    }

    public static String getDefaultRoute() {
        return "dashboard";
    }
}
