import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/** Service layer — all business logic for expenses and budget. */
public class ExpenseManager {

    private final List<Expense> expenses;
    private       int           nextId;
    private       double        monthlyBudget;

    public ExpenseManager() {
        this.expenses      = FileHandler.loadExpenses();
        this.monthlyBudget = FileHandler.loadBudget();
        this.nextId        = expenses.stream().mapToInt(Expense::getId).max().orElse(0) + 1;
    }

    private void save() { FileHandler.saveExpenses(expenses); }

    // ── Lookup ────────────────────────────────────────────────────────────────
    public Expense findById(int id) {
        return expenses.stream().filter(e -> e.getId() == id).findFirst().orElse(null);
    }

    // ── CRUD ──────────────────────────────────────────────────────────────────
    public void addExpense(double amount, String category, String description) {
        expenses.add(new Expense(nextId++, amount, category, LocalDate.now(), description));
        save();
    }

    public boolean updateExpense(int id, double amount, String category, String description) {
        Expense e = findById(id);
        if (e == null) return false;
        e.setAmount(amount);
        e.setCategory(Validator.sanitize(category));
        e.setDescription(Validator.sanitize(description));
        save();
        return true;
    }

    public boolean removeExpense(int id) {
        boolean removed = expenses.removeIf(e -> e.getId() == id);
        if (removed) save();
        return removed;
    }

    // ── Queries ───────────────────────────────────────────────────────────────
    public List<Expense> getAllExpenses() { return new ArrayList<>(expenses); }

    public List<Expense> getSortedExpenses(String sortBy) {
        Comparator<Expense> cmp = sortBy.equalsIgnoreCase("amount")
                ? Comparator.comparingDouble(Expense::getAmount)
                : Comparator.comparing(Expense::getDate);
        return expenses.stream().sorted(cmp).collect(Collectors.toList());
    }

    public List<Expense> getExpensesByMonth(int month, int year) {
        return expenses.stream()
                .filter(e -> e.getDate().getMonthValue() == month && e.getDate().getYear() == year)
                .collect(Collectors.toList());
    }

    public List<Expense> getExpensesByCategory(String category) {
        return expenses.stream()
                .filter(e -> e.getCategory().equalsIgnoreCase(category))
                .collect(Collectors.toList());
    }

    // ── Calculations ──────────────────────────────────────────────────────────
    public double calculateTotalExpenses()              { return expenses.stream().mapToDouble(Expense::getAmount).sum(); }
    public double calculateMonthlyTotal(int month, int year) { return getExpensesByMonth(month, year).stream().mapToDouble(Expense::getAmount).sum(); }

    public void   setMonthlyBudget(double b)  { this.monthlyBudget = b; FileHandler.saveBudget(b); }
    public double getMonthlyBudget()          { return monthlyBudget; }
    public double getRemainingBudget()        {
        LocalDate now = LocalDate.now();
        return monthlyBudget - calculateMonthlyTotal(now.getMonthValue(), now.getYear());
    }
}
