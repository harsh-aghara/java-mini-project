import java.time.LocalDate;

/**
 * Abstract base — holds all shared fields.
 * Implements Persistable so every subclass satisfies that interface automatically.
 */
public abstract class AbstractExpense implements Persistable {

    private int       id;
    private double    amount;
    private String    category;
    private LocalDate date;
    private String    description;

    protected AbstractExpense(int id, double amount, String category,
                              LocalDate date, String description) {
        this.id          = id;
        this.amount      = amount;
        this.category    = category;
        this.date        = date;
        this.description = description;
    }

    // Getters
    public int       getId()          { return id; }
    public double    getAmount()      { return amount; }
    public String    getCategory()    { return category; }
    public LocalDate getDate()        { return date; }
    public String    getDescription() { return description; }

    // Setters
    public void setId(int id)                   { this.id          = id; }
    public void setAmount(double amount)        { this.amount      = amount; }
    public void setCategory(String category)    { this.category    = category; }
    public void setDate(LocalDate date)         { this.date        = date; }
    public void setDescription(String desc)     { this.description = desc; }

    // getSummary() left abstract — each subclass defines its own display format
    @Override
    public abstract String getSummary();
}
