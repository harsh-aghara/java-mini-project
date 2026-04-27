import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static final ExpenseManager manager = new ExpenseManager();
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("=== Advanced Expense Tracker CLI ===");
        while (true) {
            printMenu();
            int choice = readInt("Choose an option: ");

            switch (choice) {
                case 1: addExpense(); break;
                case 2: viewAllExpenses(); break;
                case 3: updateExpense(); break;
                case 4: deleteExpense(); break;
                case 5: manageBudget(); break;
                case 6: viewSortedExpenses(); break;
                case 7: viewByMonth(); break;
                case 8: viewByCategory(); break;
                case 9:
                    System.out.println("Exiting... Goodbye!");
                    scanner.close();
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private static void printBudgetStatus() {
        LocalDate now = LocalDate.now();
        double remaining = manager.getRemainingBudget();
        double budget = manager.getMonthlyBudget();
        System.out.println("\n--- Monthly Status (" + now.getMonth() + " " + now.getYear() + ") ---");
        if (budget > 0) {
            System.out.printf("Monthly Budget: %.2f | Remaining: %.2f\n", budget, remaining);
            if (remaining < 0) {
                System.out.println("!!! WARNING: You have exceeded your budget by " + Math.abs(remaining) + " !!!");
            }
        } else {
            System.out.println("No budget set for this month.");
        }
    }

    private static void printMenu() {
        System.out.println("\n1. Add Expense");
        System.out.println("2. View All Expenses");
        System.out.println("3. Update Expense");
        System.out.println("4. Delete Expense");
        System.out.println("5. Budget Management (View/Set)");
        System.out.println("6. View Sorted Expenses");
        System.out.println("7. View by Month/Year");
        System.out.println("8. View by Category");
        System.out.println("9. Exit");
    }

    private static void addExpense() {
        double amount = readDouble("Enter amount: ");
        scanner.nextLine();
        System.out.print("Enter category: ");
        String category = scanner.nextLine();
        System.out.print("Enter description: ");
        String description = scanner.nextLine();

        manager.addExpense(amount, category, description);
        System.out.println("Expense added successfully!");
        checkBudgetWarning();
    }

    private static void updateExpense() {
        int id = readInt("Enter the ID of the expense to update: ");
        double amount = readDouble("Enter new amount: ");
        scanner.nextLine();
        System.out.print("Enter new category: ");
        String category = scanner.nextLine();
        System.out.print("Enter new description: ");
        String description = scanner.nextLine();

        if (manager.updateExpense(id, amount, category, description)) {
            System.out.println("Expense updated successfully!");
            checkBudgetWarning();
        } else {
            System.out.println("Expense ID not found.");
        }
    }

    private static void checkBudgetWarning() {
        if (manager.getMonthlyBudget() > 0) {
            double remaining = manager.getRemainingBudget();
            if (remaining < 0) {
                System.out.printf("!!! WARNING: This action caused you to exceed your budget by %.2f !!!\n", Math.abs(remaining));
            }
        }
    }

    private static void viewAllExpenses() {
        displayList(manager.getAllExpenses(), "All Expenses");
    }

    private static void deleteExpense() {
        int id = readInt("Enter the ID of the expense to delete: ");
        if (manager.removeExpense(id)) {
            System.out.println("Expense deleted successfully!");
        } else {
            System.out.println("Expense ID not found.");
        }
    }

    private static void manageBudget() {
        printBudgetStatus();
        System.out.println("\n1. Set/Update Monthly Budget");
        System.out.println("2. Return to Main Menu");
        int choice = readInt("Choice: ");
        if (choice == 1) {
            double budget = readDouble("Enter your monthly budget: ");
            manager.setMonthlyBudget(budget);
            System.out.println("Budget updated successfully!");
        }
    }

    private static void viewSortedExpenses() {
        System.out.println("Sort by: 1. Date | 2. Amount");
        int sortChoice = readInt("Choice: ");
        String sortBy = (sortChoice == 2) ? "amount" : "date";
        displayList(manager.getSortedExpenses(sortBy), "Sorted by " + sortBy);
    }

    private static void viewByMonth() {
        int month = readInt("Enter month (1-12): ");
        int year = readInt("Enter year (YYYY): ");
        displayList(manager.getExpensesByMonth(month, year), "Expenses for " + month + "/" + year);
        System.out.printf("Monthly Total: %.2f\n", manager.calculateMonthlyTotal(month, year));
    }

    private static void viewByCategory() {
        scanner.nextLine();
        System.out.print("Enter category: ");
        String category = scanner.nextLine();
        displayList(manager.getExpensesByCategory(category), "Expenses for Category: " + category);
    }

    private static void displayList(List<Expense> expenses, String title) {
        System.out.println("\n--- " + title + " ---");
        if (expenses.isEmpty()) {
            System.out.println("No records found.");
        } else {
            expenses.forEach(System.out::println);
        }
    }

    private static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Integer.parseInt(scanner.next());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
    }

    private static double readDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Double.parseDouble(scanner.next());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a decimal number.");
            }
        }
    }
}
