import java.time.LocalDate;

/**
 * INHERITANCE USED:
 *
 *  Single     : Expense extends AbstractExpense  (one direct parent)
 *  Multiple   : Expense satisfies TWO interfaces simultaneously --
 *                 Persistable  (inherited from AbstractExpense)
 *                 Taggable     (implemented directly here)
 *               Java achieves multiple inheritance through interfaces only.
 *
 *  Hierarchy:
 *    Persistable (interface)    Taggable (interface)
 *          |                        |
 *    AbstractExpense (abstract)     |
 *          |                        |
 *          +------------------------+
 *                    |
 *                 Expense  <-- concrete, satisfies both interfaces
 */
public class Expense extends AbstractExpense implements Taggable {

    // ── Static ────────────────────────────────────────────────────────────────
    private static int totalCreated = 0;

    // Column widths for table display
    private static final int W_ID   = 4;
    private static final int W_AMT  = 16;   // "INR " + 12-char number
    private static final int W_CAT  = 12;
    private static final int W_DATE = 10;

    public static final String TABLE_HEADER =
        String.format("%-" + W_ID + "s | %-" + W_AMT + "s | %-" + W_CAT + "s | %-" + W_DATE + "s | %s",
                      "ID", "Amount (INR)", "Category", "Date", "Description");

    public static final String TABLE_DIVIDER = buildDivider();

    // ── Constructor ───────────────────────────────────────────────────────────
    public Expense(int id, double amount, String category,
                   LocalDate date, String description) {
        super(id, amount, Validator.sanitize(category), date, Validator.sanitize(description));
        totalCreated++;
    }

    // ── Static helpers ────────────────────────────────────────────────────────
    public static int    getTotalCreated() { return totalCreated; }

    private static String buildDivider() {
        return TABLE_HEADER.replace(" | ", "-+-").replaceAll("[^+]", "-");
    }

    // ── Persistable (toCSVString + getSummary) ────────────────────────────────
    @Override
    public String toCSVString() {
        return getId() + "," + getAmount() + "," + getCategory()
                + "," + getDate() + "," + getDescription();
    }

    @Override
    public String getSummary() {
        String amtStr = String.format("INR %,12.2f", getAmount());   // always 16 chars
        return String.format("%" + W_ID + "d | %-" + W_AMT + "s | %-" + W_CAT + "s | %"
                + W_DATE + "s | %s",
                getId(), amtStr, getCategory(), getDate(), getDescription());
    }

    // ── Taggable ──────────────────────────────────────────────────────────────
    @Override
    public String getTag() {
        String c = getCategory().toLowerCase();
        if (c.contains("food") || c.contains("eat"))     return "[FOOD]";
        if (c.contains("travel") || c.contains("fuel"))  return "[TRAVEL]";
        if (c.contains("gym") || c.contains("health"))   return "[HEALTH]";
        if (c.contains("bill") || c.contains("util"))    return "[BILLS]";
        if (c.contains("sub"))                           return "[SUB]";
        return "[OTHER]";
    }

    // ── Factory ───────────────────────────────────────────────────────────────
    public static Expense fromCSVString(String csvLine) {
        if (csvLine == null || csvLine.trim().isEmpty()) return null;
        String[] parts = csvLine.split(",");
        if (parts.length != 5) return null;
        try {
            return new Expense(
                Integer.parseInt(parts[0].trim()),
                Double.parseDouble(parts[1].trim()),
                parts[2].trim(),
                LocalDate.parse(parts[3].trim()),
                parts[4].trim()
            );
        } catch (Exception e) {
            return null;
        }
    }

    @Override public String toString() { return getSummary(); }
}
