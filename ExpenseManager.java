import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ExpenseManager {
    private List<Expense> expenses;
    private int nextId;
    private double monthlyBudget;

    public ExpenseManager() {
        this.expenses = FileHandler.loadExpenses();
        this.monthlyBudget = FileHandler.loadBudget();
        this.nextId = calculateNextId();
    }

    private int calculateNextId() {
        return expenses.stream()
                .mapToInt(Expense::getId)
                .max()
                .orElse(0) + 1;
    }

    public void addExpense(double amount, String category, String description) {
        Expense expense = new Expense(nextId++, amount, category, LocalDate.now(), description);
        expenses.add(expense);
        save();
    }

    public boolean updateExpense(int id, double amount, String category, String description) {
        for (Expense e : expenses) {
            if (e.getId() == id) {
                e.setAmount(amount);
                e.setCategory(category);
                e.setDescription(description);
                save();
                return true;
            }
        }
        return false;
    }

    public boolean removeExpense(int id) {
        boolean removed = expenses.removeIf(e -> e.getId() == id);
        if (removed) {
            save();
        }
        return removed;
    }

    public List<Expense> getAllExpenses() {
        return new ArrayList<>(expenses);
    }

    public List<Expense> getSortedExpenses(String sortBy) {
        Comparator<Expense> comparator;
        switch (sortBy.toLowerCase()) {
            case "amount":
                comparator = Comparator.comparingDouble(Expense::getAmount);
                break;
            case "date":
            default:
                comparator = Comparator.comparing(Expense::getDate);
                break;
        }
        return expenses.stream().sorted(comparator).collect(Collectors.toList());
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

    public double calculateTotalExpenses() {
        return expenses.stream().mapToDouble(Expense::getAmount).sum();
    }

    public double calculateMonthlyTotal(int month, int year) {
        return getExpensesByMonth(month, year).stream().mapToDouble(Expense::getAmount).sum();
    }

    public void setMonthlyBudget(double budget) {
        this.monthlyBudget = budget;
        FileHandler.saveBudget(budget);
    }

    public double getMonthlyBudget() {
        return monthlyBudget;
    }

    public double getRemainingBudget() {
        LocalDate now = LocalDate.now();
        return monthlyBudget - calculateMonthlyTotal(now.getMonthValue(), now.getYear());
    }

    private void save() {
        FileHandler.saveExpenses(expenses);
    }
}
