import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FileHandler {
    private static final String FILE_NAME = "expenses.csv";
    private static final String BUDGET_FILE = "budget.txt";

    public static void saveExpenses(List<Expense> expenses) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(FILE_NAME))) {
            for (Expense expense : expenses) {
                writer.println(expense.toCSVString());
            }
        } catch (IOException e) {
            System.err.println("Error saving expenses: " + e.getMessage());
        }
    }

    public static List<Expense> loadExpenses() {
        List<Expense> expenses = new ArrayList<>();
        File file = new File(FILE_NAME);
        
        if (!file.exists()) {
            return expenses;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Expense expense = Expense.fromCSVString(line);
                if (expense != null) {
                    expenses.add(expense);
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading expenses: " + e.getMessage());
        }
        return expenses;
    }

    public static void saveBudget(double budget) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(BUDGET_FILE))) {
            writer.println(budget);
        } catch (IOException e) {
            System.err.println("Error saving budget: " + e.getMessage());
        }
    }

    public static double loadBudget() {
        File file = new File(BUDGET_FILE);
        if (!file.exists()) {
            return 0.0;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine();
            return line != null ? Double.parseDouble(line) : 0.0;
        } catch (Exception e) {
            return 0.0;
        }
    }
}
