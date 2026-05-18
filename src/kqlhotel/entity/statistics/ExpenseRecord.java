package kqlhotel.entity.statistics;

import java.time.LocalDateTime;

public class ExpenseRecord {
    private final int id;
    private final String type;
    private final String name;
    private final double amount;
    private final LocalDateTime date;
    private final String note;

    public ExpenseRecord(int id, String type, String name, double amount, LocalDateTime date, String note) {
        this.id = id;
        this.type = type;
        this.name = name;
        this.amount = amount;
        this.date = date;
        this.note = note;
    }

    public int getId() { return id; }
    public String getType() { return type; }
    public String getName() { return name; }
    public double getAmount() { return amount; }
    public LocalDateTime getDate() { return date; }
    public String getNote() { return note; }
}
