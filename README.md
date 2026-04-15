# Expense Tracker CLI 

A lightweight, persistent, and feature-rich Command Line Interface (CLI) application built with Java for managing personal finances. This project was developed as part of a Java Lab Mini Project.

## Features

-   **Full CRUD Operations**: Add, View, Update, and Delete expenses.
-   **Persistent Storage**: All data is saved to `expenses.csv` and `budget.txt` files, ensuring it remains available across sessions.
-   **Monthly Budgeting**: Set a monthly budget and receive real-time warnings if your spending exceeds your limit.
-   **Advanced Filtering**: View expenses for a specific month and year.
-   **Sorting**: Organize your expenses by **Date** or **Amount**.
-   **Input Validation**: Robust error handling for invalid data types (e.g., non-numeric amounts).

## Architecture

The application follows a modular architecture to ensure separation of concerns:

-   **`Main.java` (UI Layer)**: Handles user interaction, menu-driven navigation, and formatted console output.
-   **`ExpenseManager.java` (Service Layer)**: Contains the core business logic, including sorting, filtering, and budget calculations.
-   **`Expense.java` (Model Layer)**: Represents the data structure for an expense, including CSV serialization/deserialization logic.
-   **`FileHandler.java` (Data Access Layer)**: Manages low-level file I/O operations for both expenses and budget persistence.

## Prerequisites

-   **Java Development Kit (JDK)**: Version 8 or higher is required.
-   **Git**: For cloning the repository.

## Installation & Running

1.  **Clone the Repository**:
    ```bash
    git clone git@github.com:harsh-aghara/java_mini_project.git
    cd java_mini_project
    ```

2.  **Compile the Source Code**:
    ```bash
    javac *.java
    ```

3.  **Run the Application**:
    ```bash
    java Main
    ```

## 📂 Project Structure

```text
.
├── Expense.java        # Data model for an individual expense
├── ExpenseManager.java # Core logic and data manipulation
├── FileHandler.java    # Utility for file persistence (CSV/Txt)
├── Main.java           # Entry point and CLI menu interface
├── README.md           # Project documentation
└── .gitignore          # Ignored files (.class, data files)
```

## 📝 Usage Example

1.  **Start the app**: `java Main`
2.  **Add an expense**: Select option `1`, enter the amount (e.g., `50.0`), category (e.g., `Food`), and description (e.g., `Dinner`).
3.  **Set a budget**: Go to option `5` (Budget Management) and set your limit (e.g., `500.0`).
4.  **View sorting**: Use option `6` to sort your current spending by highest amount.

---
*Created by [harsh-aghara](https://github.com/harsh-aghara)*
