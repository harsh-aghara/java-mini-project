import java.io.*;
import java.util.ArrayList;
import java.util.List;

/** Static utility for reading and writing expenses.csv and budget.txt. */
public class FileHandler {

    private static final String EXPENSES_FILE = "expenses.csv";
    private static final String BUDGET_FILE   = "budget.txt";

    private FileHandler() {}

    public static void saveExpenses(List<Expense> expenses) {
        try (PrintWriter w = new PrintWriter(new FileWriter(EXPENSES_FILE))) {
            for (Expense e : expenses) w.println(e.toCSVString());
        } catch (IOException e) {
            System.err.println("Error saving expenses: " + e.getMessage());
        }
    }

    public static List<Expense> loadExpenses() {
        List<Expense> list = new ArrayList<>();
        File file = new File(EXPENSES_FILE);
        if (!file.exists()) return list;

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                Expense e = Expense.fromCSVString(line);
                if (e != null) list.add(e);
                else System.err.println("Warning: Skipping malformed line: " + line);
            }
        } catch (IOException e) {
            System.err.println("Error loading expenses: " + e.getMessage());
        } finally {
            if (reader != null) try { reader.close(); } catch (IOException ignored) {}
        }
        return list;
    }

    public static void saveBudget(double budget) {
        try (PrintWriter w = new PrintWriter(new FileWriter(BUDGET_FILE))) {
            w.println(budget);
        } catch (IOException e) {
            System.err.println("Error saving budget: " + e.getMessage());
        }
    }

    public static double loadBudget() {
        File file = new File(BUDGET_FILE);
        if (!file.exists()) return 0.0;
        try (BufferedReader r = new BufferedReader(new FileReader(file))) {
            String line = r.readLine();
            return line != null ? Double.parseDouble(line.trim()) : 0.0;
        } catch (Exception e) {
            return 0.0;
        }
    }
}
