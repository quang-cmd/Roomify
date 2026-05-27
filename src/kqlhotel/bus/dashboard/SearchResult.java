package kqlhotel.bus.dashboard;

/**
 * DTO dùng riêng cho Global Search.
 *
 * Một kết quả search không cần chứa toàn bộ dữ liệu của khách/phòng/hóa đơn,
 * chỉ cần đủ thông tin để hiển thị trong popup và biết phải điều hướng đến
 * màn hình nào khi người dùng click.
 */
public class SearchResult {
    // Nhóm nghiệp vụ hiển thị cho người dùng: Khách hàng, Đặt phòng, Phòng...
    private final String type;

    // Dòng chính trong popup, ví dụ: "HD014 · Nguyễn Văn A".
    private final String title;

    // Dòng mô tả phụ, ví dụ: SĐT, trạng thái, số tiền, loại phòng...
    private final String subtitle;

    // Route trong AppFrame. Click kết quả sẽ gọi navigateTo(route).
    private final String route;

    // Khóa icon/màu để UI biết render badge phù hợp từng loại kết quả.
    private final String iconKey;

    public SearchResult(String type, String title, String subtitle, String route, String iconKey) {
        this.type = type;
        this.title = title;
        this.subtitle = subtitle;
        this.route = route;
        this.iconKey = iconKey;
    }

    public String getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public String getRoute() {
        return route;
    }

    public String getIconKey() {
        return iconKey;
    }
}
