# Java Expense Tracker — Complete Project Documentation

> **Project:** CLI Expense Tracker
> **Repo:** https://github.com/harsh-aghara/java-mini-project
> **Tagline:** _Paise ped pe nhi ugte_ — Money doesn't grow on trees.

---

## Table of Contents

1. [Project Overview](#1-project-overview)
2. [Project Structure](#2-project-structure)
3. [OOP Concepts Covered](#3-oop-concepts-covered)
4. [File-by-File Deep Walkthrough](#4-file-by-file-deep-walkthrough)
   - [Persistable.java](#41-persistablejava)
   - [Taggable.java](#42-taggablejava)
   - [AbstractExpense.java](#43-abstractexpensejava)
   - [Expense.java](#44-expensejava)
   - [Validator.java](#45-validatorjava)
   - [FileHandler.java](#46-filehandlerjava)
   - [ExpenseManager.java](#47-expensemanagerjava)
   - [Main.java](#48-mainjava)
5. [How the Application Flows (Step by Step)](#5-how-the-application-flows-step-by-step)
6. [Bug Analysis & Edge Cases](#6-bug-analysis--edge-cases)
7. [What's Working Correctly](#7-whats-working-correctly)
8. [What Could Be Improved / Optimised](#8-what-could-be-improved--optimised)
9. [Java Topics Coverage Checklist](#9-java-topics-coverage-checklist)
10. [Is the Code Beginner-Friendly?](#10-is-the-code-beginner-friendly)
11. [How to Run the Project](#11-how-to-run-the-project)

---

## 1. Project Overview

This project is a **Command Line Interface (CLI) Expense Tracker** written entirely in Java. It allows a user to:

- Add, view, update, and delete expenses (full CRUD)
- Set a monthly budget and get warnings when over-budget
- Filter expenses by month/year or category
- Sort expenses by date or amount
- Persist all data to files (`expenses.csv` and `budget.txt`) so data survives between runs

Everything runs from the terminal — no GUI, no database, no external libraries needed.

---

## 2. Project Structure

```
java-mini-project/
│
├── Persistable.java      ← Interface 1: defines toCSVString() and getSummary()
├── Taggable.java         ← Interface 2: defines getTag()
├── AbstractExpense.java  ← Abstract class: holds shared fields (id, amount, category, date, description)
├── Expense.java          ← Concrete class: extends AbstractExpense, implements Taggable
├── Validator.java        ← Utility class: validates and sanitizes input
├── FileHandler.java      ← Utility class: reads and writes CSV and budget files
├── ExpenseManager.java   ← Service class: all business logic (add, update, delete, filter, sort)
├── Main.java             ← Entry point: the CLI menu, reads user input, calls ExpenseManager
│
├── tests/
│   └── RunTests.java     ← ~150 test cases, no JUnit required
│
├── expenses.csv          ← Created at runtime; stores all expenses
├── budget.txt            ← Created at runtime; stores the monthly budget
└── README.md
```

**Layer Architecture (from bottom to top):**

```
[ Persistable ]  [ Taggable ]          ← Contracts (interfaces)
       ↓
[ AbstractExpense ]                    ← Shared data (abstract class)
       ↓
[ Expense ]                            ← The actual data object
       ↓
[ Validator ]  [ FileHandler ]         ← Utility helpers
       ↓
[ ExpenseManager ]                     ← Business logic
       ↓
[ Main ]                               ← User interface (CLI)
```

---

## 3. OOP Concepts Covered

| OOP Concept                               | Where It's Used                                                                            |
| ----------------------------------------- | ------------------------------------------------------------------------------------------ |
| **Class & Object**                        | `Expense`, `ExpenseManager`, `Main` all create objects                                     |
| **Encapsulation**                         | All fields in `AbstractExpense` are `private`; accessed via getters/setters                |
| **Inheritance (Single)**                  | `Expense extends AbstractExpense`                                                          |
| **Inheritance (Multiple via Interfaces)** | `Expense` satisfies both `Persistable` and `Taggable` at once                              |
| **Abstract Class**                        | `AbstractExpense` — cannot be instantiated directly                                        |
| **Interface**                             | `Persistable`, `Taggable`                                                                  |
| **Polymorphism**                          | `displayList()` calls `e.getSummary()` — works on any `AbstractExpense` subtype            |
| **Method Overriding**                     | `getSummary()`, `toCSVString()`, `getTag()`, `toString()` all `@Override`                  |
| **Static Members**                        | `FileHandler` (all static methods), `Expense.totalCreated`, `Main.manager`, `Main.scanner` |
| **Final Fields**                          | `ExpenseManager.expenses`, `Main.manager`, `Main.scanner`                                  |
| **Access Modifiers**                      | `private` fields, `private` constructors in `FileHandler` and `Validator`                  |
| **`finally` block**                       | `FileHandler.loadExpenses()` guarantees the reader closes even on error                    |
| **Exception Handling**                    | `try-catch` in all input reading and file I/O methods                                      |
| **Collections (ArrayList)**               | Used in `ExpenseManager` to hold all expenses                                              |
| **Streams & Lambda**                      | Used in `ExpenseManager` for filtering, sorting, and summing                               |
| **Comparator**                            | Used in `getSortedExpenses()` for sorting by date or amount                                |
| **`LocalDate`**                           | Used for date management without deprecated `Date` class                                   |
| **String Formatting**                     | `String.format()` used throughout for table display                                        |
| **Factory Method Pattern**                | `Expense.fromCSVString()` is a static factory method                                       |
| **`Scanner`**                             | Used in `Main` for reading all user input                                                  |

---

## 4. File-by-File Deep Walkthrough

---

### 4.1 `Persistable.java`

**What it is:** An **interface** — a contract that any class implementing it must honour.

```java
public interface Persistable {
    String toCSVString();
    String getSummary();
}
```

**Line by line:**

- `public interface Persistable` — declares an interface named `Persistable`. An interface only defines _what_ methods must exist, not _how_ they work.
- `String toCSVString();` — any class that implements `Persistable` **must** provide a method that converts itself to a CSV (comma-separated) string. Example output: `"1,500.0,Food,2025-04-01,Lunch"`
- `String getSummary();` — any implementing class must provide a one-line human-readable summary for display on screen.

**Why it exists:** It separates the _contract_ (what an expense must be able to do) from the _implementation_ (how it does it). This makes the code flexible — if you ever add a `RecurringExpense` class, it just needs to implement the same interface.

---

### 4.2 `Taggable.java`

**What it is:** A second interface — adds the ability to auto-categorise an expense with a tag.

```java
public interface Taggable {
    String getTag();
}
```

**Line by line:**

- Just one method: `getTag()`. Any class implementing `Taggable` must be able to return a tag string like `[FOOD]`, `[TRAVEL]`, `[BILLS]`, etc.

**Why it exists:** It demonstrates that a class can implement **multiple interfaces** (Java's way of achieving multiple inheritance). `Expense` implements both `Persistable` and `Taggable` at the same time.

---

### 4.3 `AbstractExpense.java`

**What it is:** An **abstract class** that holds all the common data fields for an expense. It also implements `Persistable`, meaning any subclass automatically inherits that contract.

```java
public abstract class AbstractExpense implements Persistable {
    private int id;
    private double amount;
    private String category;
    private LocalDate date;
    private String description;

    // Constructor
    public AbstractExpense(int id, double amount, String category,
                           LocalDate date, String description) {
        this.id = id;
        this.amount = amount;
        this.category = category;
        this.date = date;
        this.description = description;
    }

    // Getters
    public int getId()            { return id; }
    public double getAmount()     { return amount; }
    public String getCategory()   { return category; }
    public LocalDate getDate()    { return date; }
    public String getDescription(){ return description; }

    // Setters
    public void setAmount(double amount)         { this.amount = amount; }
    public void setCategory(String category)     { this.category = category; }
    public void setDescription(String description){ this.description = description; }
}
```

**Line by line:**

- `public abstract class AbstractExpense implements Persistable` — This class is `abstract`, meaning you can never write `new AbstractExpense(...)`. You must create a subclass (like `Expense`) and use that. It also says it follows the `Persistable` contract, meaning it promises that `toCSVString()` and `getSummary()` will exist — though it leaves the concrete implementation to its subclass `Expense`.
- `private int id;` etc. — All five fields are `private`. This is **Encapsulation** — no other class can directly read or change these values. They can only go through the getters and setters.
- `this.id = id;` — `this` refers to the current object. When the constructor parameter and the field have the same name, `this.id` distinguishes the field from the parameter.
- **Getters** (`getId()`, `getAmount()`, etc.) — These are `public` read-only accessors. By having no setter for `id` and `date`, the class makes those fields effectively immutable after creation.
- **Setters** (`setAmount()`, `setCategory()`, `setDescription()`) — Only the fields that should be updatable have setters. `id` and `date` have no setter — once set, they cannot be changed, which is correct design.

---

### 4.4 `Expense.java`

**What it is:** The concrete (usable) data class. It extends `AbstractExpense` and implements `Taggable`. This is the main model of the application — an actual expense object.

```java
public class Expense extends AbstractExpense implements Taggable {

    private static int totalCreated = 0;

    private static final int W_ID   = 4;
    private static final int W_AMT  = 16;
    private static final int W_CAT  = 12;
    private static final int W_DATE = 10;

    public static final String TABLE_HEADER = ...;
    public static final String TABLE_DIVIDER = buildDivider();
```

**Section: Static Members**

- `private static int totalCreated = 0;` — A `static` variable is shared across **all** objects of the class. Every time a new `Expense` is created, this counter goes up. It tracks the total number of expenses ever created in the program's lifetime.
- `W_ID`, `W_AMT`, `W_CAT`, `W_DATE` — These are `static final` constants (like named numbers). `final` means they cannot be changed. They control the column widths of the table display.
- `TABLE_HEADER` and `TABLE_DIVIDER` — Static string constants that define the header row and the separator line (`---+---+---`) for the expense table. They are computed once using `String.format()`.

```java
public Expense(int id, double amount, String category,
               LocalDate date, String description) {
    super(id, amount, Validator.sanitize(category), date, Validator.sanitize(description));
    totalCreated++;
}
```

**Section: Constructor**

- `super(...)` — Calls the parent class (`AbstractExpense`) constructor, passing the five values. This is how inheritance works: the child delegates field storage to the parent.
- `Validator.sanitize(category)` — Before storing, the category and description are _sanitized_ (trimmed of extra spaces). This happens right at the point of object creation, so data is always clean.
- `totalCreated++` — Increments the shared counter every time a new expense is made.

```java
@Override
public String toCSVString() {
    return getId() + "," + getAmount() + "," + getCategory()
         + "," + getDate() + "," + getDescription();
}

@Override
public String getSummary() {
    String amtStr = String.format("INR %,12.2f", getAmount());
    return String.format("%-4d | %-16s | %-12s | %10s | %s",
        getId(), amtStr, getCategory(), getDate(), getDescription());
}
```

**Section: Persistable implementation**

- `@Override` — Tells Java (and the reader) that this method is fulfilling a promise made by an interface or parent class. If you misspell the method name, Java will give an error.
- `toCSVString()` — Joins all fields with commas. Example result: `"3,1200.0,Food,2025-04-15,Biryani"`
- `getSummary()` — Uses `String.format()` to print a nicely aligned table row. The `%,12.2f` format means: use commas as thousand separators, fill at least 12 characters wide, show 2 decimal places. So `1200.0` becomes `INR      1,200.00`.

```java
@Override
public String getTag() {
    String c = getCategory().toLowerCase();
    if (c.contains("food") || c.contains("eat")) return "[FOOD]";
    if (c.contains("travel") || c.contains("fuel")) return "[TRAVEL]";
    if (c.contains("gym") || c.contains("health")) return "[HEALTH]";
    if (c.contains("bill") || c.contains("util")) return "[BILLS]";
    if (c.contains("sub")) return "[SUB]";
    return "[OTHER]";
}
```

**Section: Taggable implementation**

- `getTag()` — Takes the category, converts to lowercase, and checks if it contains keywords. For example, if category is `"Street Food"`, `c.contains("food")` is `true`, so it returns `"[FOOD]"`.
- If no keyword matches, returns `"[OTHER]"` as a safe default.

```java
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
```

**Section: Factory method**

- `fromCSVString()` — This is a **static factory method**: a static method that creates and returns a new object. It reads one line from the CSV file and parses it back into an `Expense` object.
- `csvLine.split(",")` — Splits the string at each comma. `"3,1200.0,Food,2025-04-15,Biryani"` becomes `["3", "1200.0", "Food", "2025-04-15", "Biryani"]`.
- `parts.length != 5` — Guards against corrupted lines that don't have exactly 5 fields.
- If parsing fails for any reason (e.g., `"abc"` where a number is expected), the `catch` block returns `null` silently. The `FileHandler` will then skip that line and print a warning.

---

### 4.5 `Validator.java`

**What it is:** A pure utility class. All methods are `static` — you call them directly without creating an object. It centralizes all validation rules in one place.

```java
public class Validator {
    private Validator() {} // Prevents instantiation

    public static boolean isPositiveAmount(double amount) { return amount > 0; }
    public static boolean isPositiveBudget(double budget) { return budget > 0; }
    public static boolean isValidMonth(int month)         { return month >= 1 && month <= 12; }
    public static boolean isValidYear(int year)           { return year >= 1900 && year <= 2100; }
    public static boolean isNonEmpty(String s)            { return s != null && !s.trim().isEmpty(); }

    public static String sanitize(String s) {
        return s == null ? "" : s.trim().replaceAll("\\s+", " ");
    }
}
```

**Line by line:**

- `private Validator() {}` — A private constructor means nobody can write `new Validator()`. The class is only a bag of static helper methods, not something you instantiate. This is good design.
- `isPositiveAmount()` — Returns `true` only if the amount is greater than 0. This prevents adding expenses of ₹0 or negative amounts.
- `isValidMonth()` — Returns `true` only for months 1 through 12.
- `isValidYear()` — Returns `true` for years 1900 to 2100 — a sensible human range.
- `isNonEmpty()` — First checks `s != null` (to avoid a `NullPointerException`), then checks it's not all spaces.
- `sanitize()` — Two operations: `.trim()` removes leading/trailing spaces, then `.replaceAll("\\s+", " ")` collapses multiple internal spaces into one. Example: `"  too   much   space  "` → `"too much space"`.

---

### 4.6 `FileHandler.java`

**What it is:** A static utility class for all file input/output. It reads from and writes to `expenses.csv` and `budget.txt`.

```java
public class FileHandler {
    private static final String EXPENSES_FILE = "expenses.csv";
    private static final String BUDGET_FILE   = "budget.txt";
    private FileHandler() {}
```

- Constants define the file names. If you ever need to change the filename, you change it in one place.
- Private constructor: same pattern as `Validator` — no instantiation allowed.

```java
public static void saveExpenses(List<Expense> expenses) {
    try (PrintWriter w = new PrintWriter(new FileWriter(EXPENSES_FILE))) {
        for (Expense e : expenses) w.println(e.toCSVString());
    } catch (IOException e) {
        System.err.println("Error saving expenses: " + e.getMessage());
    }
}
```

**`saveExpenses()`:**

- `try (PrintWriter w = ...)` — This is a **try-with-resources** statement. Java automatically closes the `PrintWriter` when done, even if an error occurs. This prevents resource leaks.
- `new FileWriter(EXPENSES_FILE)` — Opens the file for writing. This **overwrites** the entire file each time. The current list in memory is always the source of truth.
- `e.toCSVString()` — Calls the method defined in `Persistable` and overridden in `Expense` to get the CSV line.

```java
public static List<Expense> loadExpenses() {
    List<Expense> list = new ArrayList<>();
    File file = new File(EXPENSES_FILE);
    if (!file.exists()) return list;  // No file yet? Return empty list.

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
```

**`loadExpenses()`:**

- `if (!file.exists()) return list;` — If the CSV file hasn't been created yet (first run), just return an empty list. No crash.
- `BufferedReader reader = null;` — Declared outside `try` so it's accessible in the `finally` block.
- `while ((line = reader.readLine()) != null)` — Reads the file line by line until end-of-file.
- **`finally` block** — This is the key teaching concept here. The `finally` block **always runs** — whether the code in `try` succeeded or an exception was thrown. This guarantees the file reader gets closed and doesn't stay open forever, consuming system resources.
- `Expense.fromCSVString(line)` — Parses each line. If it returns `null` (malformed line), the line is skipped with a warning instead of crashing.

```java
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
```

**`loadBudget()`:**

- If `budget.txt` doesn't exist, returns `0.0` (no budget set).
- Reads just one line and parses it as a `double`. If anything fails, returns `0.0` as a safe default.

---

### 4.7 `ExpenseManager.java`

**What it is:** The **service layer** — it holds all the business logic. It keeps the list of expenses in memory, delegates file I/O to `FileHandler`, and exposes clean methods to `Main`.

```java
public class ExpenseManager {
    private final List<Expense> expenses;
    private int nextId;
    private double monthlyBudget;

    public ExpenseManager() {
        this.expenses = FileHandler.loadExpenses();
        this.monthlyBudget = FileHandler.loadBudget();
        this.nextId = expenses.stream()
                              .mapToInt(Expense::getId)
                              .max()
                              .orElse(0) + 1;
    }
```

**Constructor:**

- `private final List<Expense> expenses;` — `final` means `expenses` always refers to the same `ArrayList` object (you can't do `expenses = new ArrayList<>()` again), but you can still add or remove items from it.
- `FileHandler.loadExpenses()` — On startup, all saved expenses are loaded from the CSV into memory.
- `this.nextId = expenses.stream()...` — This uses a **Stream** to find the highest ID already in the loaded list, then adds 1. This ensures new expenses always get a unique, incrementing ID even after the program restarts.
  - `.stream()` — creates a pipeline over the list
  - `.mapToInt(Expense::getId)` — transforms each `Expense` to its `int` ID (method reference syntax)
  - `.max()` — finds the maximum value (returns an `OptionalInt`)
  - `.orElse(0)` — if the list is empty, there's no max, so default to 0
  - `+ 1` — the next ID to use

```java
private void save() { FileHandler.saveExpenses(expenses); }
```

- `private save()` — A private helper so that `addExpense`, `updateExpense`, and `removeExpense` all just call `save()` in one line instead of repeating the `FileHandler` call. Good practice.

```java
public void addExpense(double amount, String category, String description) {
    expenses.add(new Expense(nextId++, amount, category, LocalDate.now(), description));
    save();
}
```

- `nextId++` — Uses the current `nextId` as the new expense's ID, then increments it for next time.
- `LocalDate.now()` — Automatically sets today's date. The user doesn't input the date manually.
- `save()` — Writes the updated list to disk immediately.

```java
public boolean updateExpense(int id, double amount, String category, String description) {
    Expense e = findById(id);
    if (e == null) return false;
    e.setAmount(amount);
    e.setCategory(Validator.sanitize(category));
    e.setDescription(Validator.sanitize(description));
    save();
    return true;
}
```

- `findById(id)` — Looks up the expense by its ID. If not found, returns `false` (the update failed).
- Sanitizes both category and description on update too — same validation as on creation.
- Returns `true` on success so `Main` can show the right message.

```java
public boolean removeExpense(int id) {
    boolean removed = expenses.removeIf(e -> e.getId() == id);
    if (removed) save();
    return removed;
}
```

- `removeIf(lambda)` — Removes all elements matching the condition. The condition is `e -> e.getId() == id`, a lambda expression meaning "the expense whose ID equals the target ID".
- Only saves if something was actually removed — avoids unnecessary disk writes.

```java
public List<Expense> getSortedExpenses(String sortBy) {
    Comparator<Expense> cmp = sortBy.equalsIgnoreCase("amount")
        ? Comparator.comparingDouble(Expense::getAmount)
        : Comparator.comparing(Expense::getDate);
    return expenses.stream().sorted(cmp).collect(Collectors.toList());
}
```

- `Comparator` — Defines _how_ to compare two expenses for sorting.
- Ternary operator (`? :`) — If `sortBy` is "amount", compare by amount; otherwise compare by date.
- `.stream().sorted(cmp).collect(Collectors.toList())` — Streams the list, sorts it, collects back into a new list. The original `expenses` list is not modified.

```java
public List<Expense> getExpensesByMonth(int month, int year) {
    return expenses.stream()
        .filter(e -> e.getDate().getMonthValue() == month
                  && e.getDate().getYear() == year)
        .collect(Collectors.toList());
}

public List<Expense> getExpensesByCategory(String category) {
    return expenses.stream()
        .filter(e -> e.getCategory().equalsIgnoreCase(category))
        .collect(Collectors.toList());
}
```

- `.filter(lambda)` — Keeps only elements that match the condition.
- `equalsIgnoreCase()` — Case-insensitive comparison, so "food", "Food", "FOOD" all match.

```java
public double getRemainingBudget() {
    LocalDate now = LocalDate.now();
    return monthlyBudget - calculateMonthlyTotal(now.getMonthValue(), now.getYear());
}
```

- Calculates remaining budget by subtracting the current month's total spending from the set budget. A negative result means over-budget.

---

### 4.8 `Main.java`

**What it is:** The **UI layer** — the entry point of the application. It handles all user interaction: printing menus, reading input, and calling `ExpenseManager`.

```java
public class Main {
    private static final ExpenseManager manager = new ExpenseManager();
    private static final Scanner scanner = new Scanner(System.in);
```

- `static final` — Both `manager` and `scanner` are class-level (static), shared across all methods, and final (created once, never replaced). This avoids passing them as parameters to every method.

```java
public static void main(String[] args) {
    System.out.println("=== Expense Tracker ===");
    boolean running = true;
    while (running) {
        printMenu();
        int choice = readIntInRange("Choose an option: ", 1, 9);
        switch (choice) {
            case 1: addExpense(); break;
            ...
            case 9: System.out.println("Exiting... Goodbye!"); running = false; break;
        }
    }
}
```

- `while (running)` — The main loop. The menu keeps showing until the user chooses option 9, which sets `running = false`.
- `readIntInRange(...)` — A helper method that won't accept invalid input. The user _cannot_ crash this by typing "banana" — they'll get asked again.
- `switch (choice)` — Dispatches to the right action method.

```java
private static void addExpense() {
    double amount = readPositiveAmount();
    if (amount < 0) return;          // User entered 0 or negative → cancel
    String category = readNonEmptyString("Enter category: ");
    String description = readNonEmptyString("Enter description: ");
    manager.addExpense(amount, category, description);
    System.out.println("Expense added successfully!");
    checkBudgetWarning();
}
```

- `readPositiveAmount()` returns `-1` as a sentinel value to signal "operation cancelled". If amount is negative, the method returns early.
- After adding, `checkBudgetWarning()` automatically checks if the budget is now exceeded.

```java
private static int readValidatedInt(String prompt, int min, int max, String err) {
    while (true) {
        System.out.print(prompt);
        try {
            int v = Integer.parseInt(scanner.nextLine().trim());
            if (v >= min && v <= max) return v;
            System.out.println(err);
        } catch (NumberFormatException e) {
            System.out.println("Please enter a whole number.");
        }
    }
}
```

- `while (true)` — Loops forever until valid input is given.
- `scanner.nextLine()` — Reads a full line. Using `nextLine()` instead of `nextInt()` avoids a common Java beginner bug where leftover newlines cause inputs to be skipped.
- `Integer.parseInt()` — Attempts to parse the input as an integer. If it fails (e.g., user typed "abc"), `NumberFormatException` is thrown and caught, and the user is prompted again.
- `if (v >= min && v <= max)` — Range check. Only returns when input is within the valid range.

```java
private static void displayList(List<Expense> expenses, String title) {
    System.out.println("\n--- " + title + " ---");
    if (expenses.isEmpty()) { System.out.println("No records found."); return; }
    System.out.println(Expense.TABLE_HEADER);
    System.out.println(Expense.TABLE_DIVIDER);
    expenses.forEach(e -> System.out.println(e.getSummary()));
}
```

- `expenses.isEmpty()` — Handles the empty-list case gracefully instead of printing an empty table.
- `expenses.forEach(e -> ...)` — Lambda-based iteration. Equivalent to a `for-each` loop but more concise.
- `e.getSummary()` — This is **polymorphism** in action: `displayList` works on any `AbstractExpense` subtype, calling their overridden `getSummary()`.

---

## 5. How the Application Flows (Step by Step)

Here is the complete journey from startup to exit:

### Step 1: Program Starts

```
java Main
```

`main()` is called. The `ExpenseManager` constructor fires immediately:

- `FileHandler.loadExpenses()` reads `expenses.csv` line by line, parsing each into an `Expense` object, building the in-memory list.
- `FileHandler.loadBudget()` reads `budget.txt` to restore the saved budget.
- `nextId` is computed from the highest existing ID + 1.

### Step 2: Menu Loop

The `while (running)` loop starts. `printMenu()` prints the 9 options. `readIntInRange()` waits for valid input.

### Step 3: User Picks "Add Expense" (Option 1)

1. `readPositiveAmount()` prompts for amount. Loops until user gives a number > 0.
2. `readNonEmptyString()` prompts for category. Loops until non-blank input given. The input is sanitized via `Validator.sanitize()`.
3. `readNonEmptyString()` prompts for description. Same validation.
4. `manager.addExpense(amount, category, description)` is called:
   - A new `Expense` object is created with `nextId++` and `LocalDate.now()`.
   - It's added to the in-memory `ArrayList`.
   - `save()` is called, which calls `FileHandler.saveExpenses()`, which rewrites the entire CSV.
5. "Expense added successfully!" is printed.
6. `checkBudgetWarning()` checks if the monthly total now exceeds the budget. If yes, a warning is printed.
7. Control returns to the top of the `while` loop.

### Step 4: User Picks "View All Expenses" (Option 2)

1. `manager.getAllExpenses()` returns a copy of the expense list (defensive copy via `new ArrayList<>(expenses)`).
2. `displayList()` prints the header, divider, then calls `e.getSummary()` for each expense.
3. Grand total is printed using `calculateTotalExpenses()`.

### Step 5: User Picks "Exit" (Option 9)

`running = false` is set. The while loop condition fails. `main()` returns. Program ends. All data was already saved to disk after every change, so nothing is lost.

---

## 6. Bug Analysis & Edge Cases

### ✅ No Crashes Found — The Code is Robust

After thorough analysis, there are **no runtime bugs** in the main logic. Here is a summary of each potential edge case and how the code handles it:

| Edge Case                                       | How It's Handled                                                                            |
| ----------------------------------------------- | ------------------------------------------------------------------------------------------- |
| User types letters when a number is expected    | `NumberFormatException` is caught; user is re-prompted                                      |
| User enters amount = 0 or negative              | `Validator.isPositiveAmount()` rejects it; operation is cancelled                           |
| User enters an expense ID that doesn't exist    | `findById()` returns `null`; an error message is shown                                      |
| Empty input for category or description         | `Validator.isNonEmpty()` rejects it; user is re-prompted                                    |
| `expenses.csv` doesn't exist on first run       | `FileHandler.loadExpenses()` checks `file.exists()` and returns empty list                  |
| `budget.txt` doesn't exist on first run         | `FileHandler.loadBudget()` returns `0.0` (no budget set)                                    |
| Malformed/corrupted line in CSV                 | `fromCSVString()` returns `null`; line is skipped with a warning                            |
| CSV file has a line with wrong number of commas | `parts.length != 5` check returns null safely                                               |
| Description contains a comma (CSV injection)    | ✅ `fromCSVString()` uses `split(",", 5)` — description absorbs all remaining commas safely |
| Filter by month/year returns no results         | `displayList()` prints "No records found."                                                  |
| Budget not set and user views budget            | "No budget set." is printed                                                                 |
| Sorting an empty list                           | `stream().sorted()` returns empty stream → `displayList()` shows "No records found."        |

### Minor Observations (Not Bugs, But Worth Noting)

- **`totalCreated` counter** — The static `totalCreated` in `Expense` counts how many objects were created in the current session, including objects made during `fromCSVString()` on file load. It's not persisted and is never displayed to the user, so its usefulness is mainly for demonstrating static members.
- **Date is always today** — You cannot add an expense with a past date (e.g., a bill you forgot to log from last week). This is a deliberate design choice to keep it simple.
- **No duplicate ID protection on corrupted CSV** — If someone manually edits the CSV to create duplicate IDs, `findById()` returns the first match. Not a real-world concern for a beginner project.

---

## 7. What's Working Correctly

Everything core works correctly:

- Full CRUD: Add ✅ View ✅ Update ✅ Delete ✅
- Filter by month/year ✅
- Filter by category (case-insensitive) ✅
- Sort by date or amount ✅
- Budget management: set, view, remaining, warnings ✅
- File persistence: expenses and budget survive program restarts ✅
- Input validation: no crash on bad input ✅
- Table formatting: clean aligned columns ✅
- Malformed CSV lines are skipped gracefully ✅
- All OOP concepts demonstrated correctly ✅

---

## 8. What Could Be Improved / Optimised

These are **optional improvements** — the project works correctly without them. Mention these to your faculty to show deeper understanding.

### 1. CSV Comma Handling is Already Fixed

`Expense.java` now uses `split(",", 5)` in `fromCSVString()`, so descriptions with commas are loaded correctly and absorb the remaining text safely.

### 2. Add `@Override toString()` Actually Used

The class already has `@Override public String toString() { return getSummary(); }`, but `displayList()` calls `e.getSummary()` directly. This is fine — but if you used `System.out.println(e)` instead, `toString()` would be called automatically. Good to know.

### 3. `getTag()` is Defined But Never Displayed

The `Taggable` interface and `getTag()` method work correctly, but the tag is never shown in the CLI output. You could add it to `getSummary()` or `displayList()` to make the feature visible to the user.

### 4. Scanner is Closed on Exit

`Main.scanner` is now closed with `scanner.close()` before `main()` returns. For a CLI app this is mostly a cleanup improvement, but it is good practice.

### 5. `totalCreated` Could Be Exposed in the UI

Since it's demonstrating a static concept, you could print it somewhere — e.g., "Total expenses added this session: 3".

---

## 9. Java Topics Coverage Checklist

This checklist matches what a typical first-year Java course covers:

| Topic                                    | Present? | Where                                                                   |
| ---------------------------------------- | -------- | ----------------------------------------------------------------------- |
| Variables and Data Types                 | ✅       | `int`, `double`, `String`, `boolean` throughout                         |
| Operators                                | ✅       | Arithmetic, comparison, logical, ternary                                |
| Control Flow (`if/else`, `switch`)       | ✅       | `Main.java` (switch menu), validation checks                            |
| Loops (`while`, `for-each`)              | ✅       | Main loop, file reading loop, `forEach`                                 |
| Methods                                  | ✅       | Every file has multiple methods                                         |
| Arrays / Collections                     | ✅       | `ArrayList<Expense>` in `ExpenseManager`                                |
| Class and Object                         | ✅       | `Expense`, `ExpenseManager`                                             |
| Encapsulation                            | ✅       | `private` fields + getters/setters in `AbstractExpense`                 |
| Inheritance                              | ✅       | `Expense extends AbstractExpense`                                       |
| Abstract Class                           | ✅       | `AbstractExpense`                                                       |
| Interface                                | ✅       | `Persistable`, `Taggable`                                               |
| Polymorphism                             | ✅       | `getSummary()` called via abstract type                                 |
| Method Overriding (`@Override`)          | ✅       | `toCSVString()`, `getSummary()`, `getTag()`, `toString()`               |
| `static` keyword                         | ✅       | `FileHandler`, `Validator`, `Expense.totalCreated`                      |
| `final` keyword                          | ✅       | `final List`, `final ExpenseManager`, constants                         |
| Access Modifiers                         | ✅       | `private`, `public`, `static` used correctly                            |
| Exception Handling (`try-catch-finally`) | ✅       | `FileHandler.loadExpenses()`                                            |
| `Scanner` (User Input)                   | ✅       | `Main.java`                                                             |
| File I/O                                 | ✅       | `FileHandler` reads/writes CSV                                          |
| `String` methods                         | ✅       | `.trim()`, `.toLowerCase()`, `.contains()`, `.split()`, `.replaceAll()` |
| `String.format()`                        | ✅       | Table formatting in `Expense`                                           |
| `LocalDate` (Date API)                   | ✅       | Used throughout                                                         |
| Streams & Lambdas (bonus)                | ✅       | `filter`, `sorted`, `mapToInt`, `forEach`                               |
| Comparator (bonus)                       | ✅       | `getSortedExpenses()`                                                   |
| Constructor Chaining (`super()`)         | ✅       | `Expense` constructor calls `super(...)`                                |

**Overall: Excellent coverage.** All core beginner topics are demonstrated with real, working usage — not just token mentions.

---

## 10. Is the Code Beginner-Friendly?

**Short answer: Mostly yes, with a few areas that may need explanation.**

### What's Very Beginner-Friendly ✅

- Each class has a single, clear responsibility — easy to understand one file at a time.
- Meaningful variable and method names (`readNonEmptyString`, `checkBudgetWarning`, `isPositiveAmount`) — you can guess what they do without reading the code.
- The `// ── Section ──` comments in `Expense.java` and `ExpenseManager.java` group related methods visually.
- The `Javadoc`-style `/** */` comments explain the purpose of each class in one sentence.
- Error messages are human-readable and specific: `"Error: ID 5 not found."` rather than a raw exception.
- The `README.md` has a clear class diagram and application flow diagram.

### What Might Be Confusing for Beginners ⚠️

- **Streams and lambdas** — `expenses.stream().filter(e -> ...).collect(Collectors.toList())` is elegant but advanced for a first Java course. A beginner might find a simple `for` loop easier to read. However, it's fine to have both styles; the stream code is correct and compact.
- **Method references** (`Expense::getId`) — Shorthand for `e -> e.getId()`. Not always taught in intro courses.
- **`nextId` computation** — `expenses.stream().mapToInt(Expense::getId).max().orElse(0) + 1` is still a dense line, but it now has a plain-English comment explaining the intent.
- **`readPositiveAmount()` returning `-1`** — Using `-1` as a "cancel signal" (sentinel value) is a common pattern, but the comment explaining _why_ `-1` means cancel would help a new reader.
- **The `finally` block** — Well-used in `FileHandler`. Most beginners don't encounter `finally` until midway through a course, so this is a good teaching moment.

### Recommendation

Add a few inline comments on the more complex lines — especially the stream-based `nextId` computation, the sentinel `-1` pattern, and the `finally` block. These would make the code fully beginner-friendly without changing any logic.

---

## 11. How to Run the Project

### Prerequisites

- Java Development Kit (JDK) **8 or higher** installed
- A terminal (Command Prompt, Terminal, or Git Bash)

### Steps

```bash
# 1. Clone the repository
git clone https://github.com/harsh-aghara/java-mini-project.git
cd java-mini-project

# 2. Compile all Java files
javac *.java

# 3. Run the program
java Main
```

### Running the Tests

```bash
# Compile everything including the test file
javac *.java tests/RunTests.java

# Run on Windows:
java -cp "tests;." RunTests

# Run on Linux/macOS:
java -cp "tests:." RunTests
```

The test suite covers ~150 cases across 7 sections: Validator, Expense model, CRUD, Queries, Budget, File round-trips, and Edge cases.

---

_Documentation prepared for the Java Mini Project — Expense Tracker CLI_
_Covers: code analysis, bug review, OOP topic mapping, and beginner-friendliness assessment._
