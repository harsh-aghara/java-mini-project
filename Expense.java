import java.time.LocalDate;

public class Expense {
    private int id;
    private double amount;
    private String category;
    private LocalDate date;
    private String description;

    public Expense(int id, double amount, String category, LocalDate date, String description) {
        this.id = id;
        this.amount = amount;
        this.category = category;
        this.date = date;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String toCSVString() {
        return id + "," + amount + "," + category + "," + date + "," + description;
    }

    public static Expense fromCSVString(String csvLine) {
        String[] parts = csvLine.split(",");
        if (parts.length != 5) return null;
        try {
            int id = Integer.parseInt(parts[0]);
            double amount = Double.parseDouble(parts[1]);
            String category = parts[2];
            LocalDate date = LocalDate.parse(parts[3]);
            String description = parts[4];
            return new Expense(id, amount, category, date, description);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public String toString() {
        return String.format("ID: %d | Amount: %.2f | Category: %-10s | Date: %s | Description: %s",
                id, amount, category, date, description);
    }
}
