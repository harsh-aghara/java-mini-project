/**
 * Centralised input validation — all static, no instantiation.
 */
public class Validator {

    private Validator() {}

    public static boolean isPositiveAmount(double amount)  { return amount > 0; }
    public static boolean isPositiveBudget(double budget)  { return budget > 0; }
    public static boolean isValidMonth(int month)          { return month >= 1 && month <= 12; }
    public static boolean isValidYear(int year)            { return year >= 1900 && year <= 2100; }
    public static boolean isNonEmpty(String s)             { return s != null && !s.trim().isEmpty(); }

    /** Trims and collapses internal whitespace runs to a single space. */
    public static String sanitize(String s) {
        return s == null ? "" : s.trim().replaceAll("\\s+", " ");
    }
}
