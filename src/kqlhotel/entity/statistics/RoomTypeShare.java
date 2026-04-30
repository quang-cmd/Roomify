package kqlhotel.entity.statistics;

/** 1 lát của biểu đồ tròn phân bố loại phòng. */
public class RoomTypeShare {
    private final String label;
    private final int count;

    public RoomTypeShare(String label, int count) {
        this.label = label;
        this.count = count;
    }

    public String getLabel() { return label; }
    public int    getCount() { return count; }
}
