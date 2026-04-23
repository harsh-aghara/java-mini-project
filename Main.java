import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

public class Main {

    private static final ExpenseManager manager = new ExpenseManager();
    private static final Scanner        scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("=== Expense Tracker ===");
        boolean running = true;
        while (running) {
            printMenu();
            int choice = readIntInRange("Choose an option: ", 1, 9);
            switch (choice) {
                case 1: addExpense();         break;
                case 2: viewAllExpenses();    break;
                case 3: updateExpense();      break;
                case 4: deleteExpense();      break;
                case 5: manageBudget();       break;
                case 6: viewSortedExpenses(); break;
                case 7: viewByMonth();        break;
                case 8: viewByCategory();     break;
                case 9: System.out.println("Exiting... Goodbye!"); running = false; break;
            }
        }
    }

    // ── Menu ──────────────────────────────────────────────────────────────────
    private static void printMenu() {
        System.out.println("\n1. Add Expense");
        System.out.println("2. View All Expenses");
        System.out.println("3. Update Expense");
        System.out.println("4. Delete Expense");
        System.out.println("5. Budget Management");
        System.out.println("6. View Sorted Expenses");
        System.out.println("7. View by Month/Year");
        System.out.println("8. View by Category");
        System.out.println("9. Exit");
    }

    // ── Expense actions ───────────────────────────────────────────────────────
    private static void addExpense() {
        double amount = readPositiveAmount();
        if (amount < 0) return;
        String category    = readNonEmptyString("Enter category: ");
        String description = readNonEmptyString("Enter description: ");
        manager.addExpense(amount, category, description);
        System.out.println("Expense added successfully!");
        checkBudgetWarning();
    }

    private static void updateExpense() {
        int id = readInt("Enter expense ID to update: ");
        if (manager.findById(id) == null) { System.out.println("Error: ID " + id + " not found."); return; }
        double amount = readPositiveAmount();
        if (amount < 0) return;
        String category    = readNonEmptyString("Enter new category: ");
        String description = readNonEmptyString("Enter new description: ");
        manager.updateExpense(id, amount, category, description);
        System.out.println("Expense updated successfully!");
        checkBudgetWarning();
    }

    private static void deleteExpense() {
        int id = readInt("Enter expense ID to delete: ");
        System.out.println(manager.removeExpense(id)
                ? "Expense deleted successfully!"
                : "Error: ID " + id + " not found.");
    }

    // ── View actions ──────────────────────────────────────────────────────────
    private static void viewAllExpenses() {
        displayList(manager.getAllExpenses(), "All Expenses");
        System.out.printf("Grand Total: INR %,12.2f%n", manager.calculateTotalExpenses());
    }

    private static void viewSortedExpenses() {
        System.out.println("Sort by: 1. Date  2. Amount");
        String sortBy = (readIntInRange("Choice: ", 1, 2) == 2) ? "amount" : "date";
        displayList(manager.getSortedExpenses(sortBy), "Sorted by " + sortBy);
    }

    private static void viewByMonth() {
        int month = readValidatedInt("Enter month (1-12): ",   1,    12,   "Month must be 1-12.");
        int year  = readValidatedInt("Enter year (1900-2100):", 1900, 2100, "Year must be 1900-2100.");
        displayList(manager.getExpensesByMonth(month, year), "Expenses for " + month + "/" + year);
        System.out.printf("Monthly Total: INR %,12.2f%n", manager.calculateMonthlyTotal(month, year));
    }

    private static void viewByCategory() {
        String category = readNonEmptyString("Enter category: ");
        displayList(manager.getExpensesByCategory(category), "Category: " + category);
    }

    // ── Budget ────────────────────────────────────────────────────────────────
    private static void manageBudget() {
        printBudgetStatus();
        System.out.println("\n1. Set/Update Budget  2. Return");
        if (readIntInRange("Choice: ", 1, 2) == 1) {
            double budget = readPositiveBudget();
            if (budget < 0) return;
            manager.setMonthlyBudget(budget);
            System.out.println("Budget updated successfully!");
        }
    }

    private static void printBudgetStatus() {
        LocalDate now = LocalDate.now();
        double budget = manager.getMonthlyBudget(), remaining = manager.getRemainingBudget();
        System.out.println("\n--- Monthly Status (" + now.getMonth() + " " + now.getYear() + ") ---");
        if (budget > 0) {
            System.out.printf("Budget: INR %,12.2f | Remaining: INR %,12.2f%n", budget, remaining);
            if (remaining < 0) System.out.printf("WARNING: Over budget by INR %,.2f!%n", Math.abs(remaining));
        } else {
            System.out.println("No budget set.");
        }
    }

    private static void checkBudgetWarning() {
        if (manager.getMonthlyBudget() > 0 && manager.getRemainingBudget() < 0)
            System.out.printf("WARNING: Over budget by INR %,.2f!%n", Math.abs(manager.getRemainingBudget()));
    }

    // ── Display ───────────────────────────────────────────────────────────────
    private static void displayList(List<Expense> expenses, String title) {
        System.out.println("\n--- " + title + " ---");
        if (expenses.isEmpty()) { System.out.println("No records found."); return; }
        System.out.println(Expense.TABLE_HEADER);
        System.out.println(Expense.TABLE_DIVIDER);
        expenses.forEach(e -> System.out.println(e.getSummary()));
    }

    // ── Input helpers ─────────────────────────────────────────────────────────
    private static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try { return Integer.parseInt(scanner.nextLine().trim()); }
            catch (NumberFormatException e) { System.out.println("Please enter a whole number."); }
        }
    }

    private static int readIntInRange(String prompt, int min, int max) {
        return readValidatedInt(prompt, min, max, "Please enter a number between " + min + " and " + max + ".");
    }

    private static int readValidatedInt(String prompt, int min, int max, String err) {
        while (true) {
            System.out.print(prompt);
            try {
                int v = Integer.parseInt(scanner.nextLine().trim());
                if (v >= min && v <= max) return v;
                System.out.println(err);
            } catch (NumberFormatException e) { System.out.println("Please enter a whole number."); }
        }
    }

    private static double readPositiveAmount() {
        while (true) {
            System.out.print("Enter amount: ");
            try {
                double v = Double.parseDouble(scanner.nextLine().trim());
                if (Validator.isPositiveAmount(v)) return v;
                System.out.println("Amount must be greater than 0. Operation cancelled.");
                return -1;
            } catch (NumberFormatException e) { System.out.println("Please enter a number."); }
        }
    }

    private static double readPositiveBudget() {
        while (true) {
            System.out.print("Enter budget: ");
            try {
                double v = Double.parseDouble(scanner.nextLine().trim());
                if (Validator.isPositiveBudget(v)) return v;
                System.out.println("Budget must be greater than 0. Operation cancelled.");
                return -1;
            } catch (NumberFormatException e) { System.out.println("Please enter a number."); }
        }
    }

    private static String readNonEmptyString(String prompt) {
        while (true) {
            System.out.print(prompt);
            String val = Validator.sanitize(scanner.nextLine());
            if (Validator.isNonEmpty(val)) return val;
            System.out.println("Input cannot be empty.");
        }
    }
}
