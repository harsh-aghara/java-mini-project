import java.time.LocalDate;
import java.util.List;

/**
 * Self-contained test suite — no JUnit needed (JDK 8+).
 * Compile & run from project root:
 *   javac *.java tests/RunTests.java
 *   java -cp "tests;." RunTests        (Windows)
 *   java -cp "tests:." RunTests        (Linux/macOS)
 *
 * NOTE: ExpenseManager tests use the real expenses.csv / budget.txt.
 *       Each test restores the state it modifies (cleanup via removeExpense / setMonthlyBudget).
 */
public class RunTests {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        System.out.println("=== Expense Tracker – Full Test Suite ===\n");

        // ── A: Validator ──────────────────────────────────────────────────────
        section("A  Validator – amount");
        assertTrue("A01 positive amount 100.0",            Validator.isPositiveAmount(100.0));
        assertTrue("A02 positive amount 0.001 (min boundary)", Validator.isPositiveAmount(0.001));
        assertTrue("A03 positive amount Double.MAX_VALUE", Validator.isPositiveAmount(Double.MAX_VALUE));
        assertFalse("A04 zero amount",                     Validator.isPositiveAmount(0.0));
        assertFalse("A05 negative amount -0.001",          Validator.isPositiveAmount(-0.001));
        assertFalse("A06 negative amount -999",            Validator.isPositiveAmount(-999));

        section("A  Validator – budget");
        assertTrue("A07 positive budget 5000",             Validator.isPositiveBudget(5000));
        assertTrue("A08 positive budget 0.01",             Validator.isPositiveBudget(0.01));
        assertFalse("A09 zero budget",                     Validator.isPositiveBudget(0));
        assertFalse("A10 negative budget",                 Validator.isPositiveBudget(-1));

        section("A  Validator – month");
        assertTrue("A11 month 1",                          Validator.isValidMonth(1));
        assertTrue("A12 month 6",                          Validator.isValidMonth(6));
        assertTrue("A13 month 12",                         Validator.isValidMonth(12));
        assertFalse("A14 month 0",                         Validator.isValidMonth(0));
        assertFalse("A15 month 13",                        Validator.isValidMonth(13));
        assertFalse("A16 month -1",                        Validator.isValidMonth(-1));
        assertFalse("A17 month 30",                        Validator.isValidMonth(30));
        assertFalse("A18 month 100",                       Validator.isValidMonth(100));

        section("A  Validator – year");
        assertTrue("A19 year 1900 (lower boundary)",       Validator.isValidYear(1900));
        assertTrue("A20 year 2026 (current)",              Validator.isValidYear(2026));
        assertTrue("A21 year 2100 (upper boundary)",       Validator.isValidYear(2100));
        assertFalse("A22 year 1899",                       Validator.isValidYear(1899));
        assertFalse("A23 year 2101",                       Validator.isValidYear(2101));
        assertFalse("A24 year 0",                          Validator.isValidYear(0));
        assertFalse("A25 year 2",                          Validator.isValidYear(2));
        assertFalse("A26 year -1",                         Validator.isValidYear(-1));

        section("A  Validator – sanitize");
        assertEqual("A27 trailing spaces",        "gym",       Validator.sanitize("gym   "));
        assertEqual("A28 leading spaces",         "gym",       Validator.sanitize("   gym"));
        assertEqual("A29 internal multi-spaces",  "gym class", Validator.sanitize("gym   class"));
        assertEqual("A30 null → empty string",    "",          Validator.sanitize(null));
        assertEqual("A31 all-blank → empty",      "",          Validator.sanitize("   "));
        assertEqual("A32 single char",            "a",         Validator.sanitize("a"));
        assertEqual("A33 mixed case preserved",   "Food",      Validator.sanitize("  Food  "));
        assertEqual("A34 tab treated as space",   "a b",       Validator.sanitize("a\tb"));

        section("A  Validator – isNonEmpty");
        assertTrue("A35 normal word",              Validator.isNonEmpty("food"));
        assertTrue("A36 single char",             Validator.isNonEmpty("x"));
        assertFalse("A37 null",                   Validator.isNonEmpty(null));
        assertFalse("A38 empty string",           Validator.isNonEmpty(""));
        assertFalse("A39 only spaces",            Validator.isNonEmpty("   "));
        assertFalse("A40 only tab",               Validator.isNonEmpty("\t"));

        // ── B: Expense model ──────────────────────────────────────────────────
        section("B  Expense – construction & getters");
        LocalDate today = LocalDate.now();
        Expense e = new Expense(5, 250.50, "Food", today, "Dinner");
        assertEqual("B01 getId",          "5",      String.valueOf(e.getId()));
        assertEqual("B02 getAmount",      "250.5",  String.valueOf(e.getAmount()));
        assertEqual("B03 getCategory",    "Food",   e.getCategory());
        assertEqual("B04 getDate",        today.toString(), e.getDate().toString());
        assertEqual("B05 getDescription", "Dinner", e.getDescription());

        section("B  Expense – setters");
        Expense s = new Expense(1, 10, "Old", today, "old desc");
        s.setAmount(99.99);
        s.setCategory("New");
        s.setDescription("new desc");
        s.setId(42);
        assertTrue("B06 setAmount",      s.getAmount() == 99.99);
        assertEqual("B07 setCategory",   "New",     s.getCategory());
        assertEqual("B08 setDescription","new desc", s.getDescription());
        assertEqual("B09 setId",         "42",      String.valueOf(s.getId()));

        section("B  Expense – sanitization on construction");
        Expense san = new Expense(1, 100, "  gym   ", today, "  lifting  ");
        assertEqual("B10 category sanitized",    "gym",      san.getCategory());
        assertEqual("B11 description sanitized", "lifting",  san.getDescription());
        Expense multiSpace = new Expense(1, 100, "gym  class", today, "heavy  weight");
        assertEqual("B12 category internal spaces collapsed", "gym class",    multiSpace.getCategory());
        assertEqual("B13 description internal spaces collapsed","heavy weight", multiSpace.getDescription());

        section("B  Expense – toCSVString");
        Expense csv = new Expense(3, 500, "Travel", LocalDate.of(2026,1,15), "flight");
        String line = csv.toCSVString();
        assertTrue("B14 toCSVString has exactly 4 commas", line.chars().filter(c -> c == ',').count() == 4);
        assertTrue("B15 toCSVString starts with id",       line.startsWith("3,"));
        assertTrue("B16 toCSVString contains amount",      line.contains("500.0"));
        assertTrue("B17 toCSVString contains category",    line.contains("Travel"));
        assertTrue("B18 toCSVString contains date",        line.contains("2026-01-15"));
        assertTrue("B19 toCSVString contains description", line.contains("flight"));

        section("B  Expense – fromCSVString");
        assertTrue("B20 valid line returns non-null",
                Expense.fromCSVString("1,250.0,Food,2026-04-22,Dinner") != null);
        assertFalse("B21 null input returns null",
                Expense.fromCSVString(null) != null);
        assertFalse("B22 empty string returns null",
                Expense.fromCSVString("") != null);
        assertFalse("B23 blank string returns null",
                Expense.fromCSVString("   ") != null);
        assertFalse("B24 too few columns returns null",
                Expense.fromCSVString("1,250.0,Food,2026-04-22") != null);
        assertFalse("B25 too many columns returns null",
                Expense.fromCSVString("1,250.0,Food,2026-04-22,Dinner,Extra") != null);
        assertFalse("B26 non-numeric amount returns null",
                Expense.fromCSVString("1,abc,Food,2026-04-22,Dinner") != null);
        assertFalse("B27 non-numeric id returns null",
                Expense.fromCSVString("xyz,250.0,Food,2026-04-22,Dinner") != null);
        assertFalse("B28 bad date format returns null",
                Expense.fromCSVString("1,250.0,Food,22-04-2026,Dinner") != null);

        section("B  Expense – CSV round-trip");
        Expense orig = new Expense(7, 1234.56, "Bills", LocalDate.of(2026, 3, 10), "electricity");
        Expense rt   = Expense.fromCSVString(orig.toCSVString());
        assertTrue("B29 round-trip returns non-null", rt != null);
        if (rt != null) {
            assertEqual("B30 round-trip id",          "7",           String.valueOf(rt.getId()));
            assertEqual("B31 round-trip amount",      "1234.56",     String.valueOf(rt.getAmount()));
            assertEqual("B32 round-trip category",    "Bills",       rt.getCategory());
            assertEqual("B33 round-trip date",        "2026-03-10",  rt.getDate().toString());
            assertEqual("B34 round-trip description", "electricity", rt.getDescription());
        }

        section("B  Expense – getSummary & toString");
        Expense disp = new Expense(2, 500, "Food", today, "lunch");
        String summary = disp.getSummary();
        assertTrue("B35 getSummary contains INR",         summary.contains("INR"));
        assertTrue("B36 getSummary contains category",    summary.contains("Food"));
        assertTrue("B37 getSummary contains description", summary.contains("lunch"));
        assertTrue("B38 getSummary contains date",        summary.contains(today.toString()));
        assertEqual("B39 toString equals getSummary",     summary, disp.toString());

        section("B  Expense – table header & divider");
        assertTrue("B40 TABLE_HEADER not null",           Expense.TABLE_HEADER != null);
        assertTrue("B41 TABLE_HEADER not empty",          !Expense.TABLE_HEADER.isEmpty());
        assertTrue("B42 TABLE_HEADER contains ID",        Expense.TABLE_HEADER.contains("ID"));
        assertTrue("B43 TABLE_HEADER contains INR",       Expense.TABLE_HEADER.contains("INR"));
        assertTrue("B44 TABLE_HEADER contains Category",  Expense.TABLE_HEADER.contains("Category"));
        assertTrue("B45 TABLE_HEADER contains Date",      Expense.TABLE_HEADER.contains("Date"));
        assertTrue("B46 TABLE_HEADER contains Description",Expense.TABLE_HEADER.contains("Description"));
        assertTrue("B47 TABLE_DIVIDER not null",          Expense.TABLE_DIVIDER != null);
        assertTrue("B48 TABLE_DIVIDER not empty",         !Expense.TABLE_DIVIDER.isEmpty());
        assertTrue("B49 TABLE_HEADER and DIVIDER same length",
                Expense.TABLE_HEADER.length() == Expense.TABLE_DIVIDER.length());

        section("B  Expense – static counter");
        int before = Expense.getTotalCreated();
        new Expense(999, 1, "Count", today, "test");
        assertEqual("B50 totalCreated increments", String.valueOf(before + 1),
                String.valueOf(Expense.getTotalCreated()));

        // ── C: ExpenseManager – CRUD ──────────────────────────────────────────
        section("C  ExpenseManager – add & find");
        ExpenseManager m = new ExpenseManager();

        int sizeBefore = m.getAllExpenses().size();
        m.addExpense(100, "TestC", "C01");
        assertEqual("C01 add increases size by 1",
                String.valueOf(sizeBefore + 1), String.valueOf(m.getAllExpenses().size()));

        Expense last = lastOf(m);
        assertTrue("C02 findById after add", m.findById(last.getId()) != null);
        assertEqual("C03 findById returns correct amount",
                "100.0", String.valueOf(m.findById(last.getId()).getAmount()));
        m.removeExpense(last.getId());

        section("C  ExpenseManager – add two, both findable");
        m.addExpense(10, "CatA", "first");
        m.addExpense(20, "CatB", "second");
        Expense second = lastOf(m);
        m.addExpense(30, "CatC", "third");
        Expense third = lastOf(m);
        assertTrue("C04 second expense findable", m.findById(second.getId()) != null);
        assertTrue("C05 third expense findable",  m.findById(third.getId()) != null);
        m.removeExpense(second.getId());
        m.removeExpense(third.getId());
        // also remove the "first" one
        m.removeExpense(lastOf(m).getId());

        section("C  ExpenseManager – update");
        m.addExpense(50, "BeforeCat", "beforeDesc");
        Expense upd = lastOf(m);
        boolean updResult = m.updateExpense(upd.getId(), 99, "AfterCat", "afterDesc");
        assertTrue("C06 updateExpense returns true for existing id", updResult);
        Expense updated = m.findById(upd.getId());
        assertTrue("C07 updated amount correct",      updated != null && updated.getAmount() == 99);
        assertEqual("C08 updated category correct",   "AfterCat",   updated != null ? updated.getCategory() : "");
        assertEqual("C09 updated description correct","afterDesc",  updated != null ? updated.getDescription() : "");
        m.removeExpense(upd.getId());

        section("C  ExpenseManager – update sanitizes strings");
        m.addExpense(5, "Raw", "raw");
        Expense rawE = lastOf(m);
        m.updateExpense(rawE.getId(), 5, "  spaced  ", "  spaced desc  ");
        Expense sanitized = m.findById(rawE.getId());
        assertEqual("C10 category sanitized on update",    "spaced",       sanitized != null ? sanitized.getCategory() : "");
        assertEqual("C11 description sanitized on update", "spaced desc",  sanitized != null ? sanitized.getDescription() : "");
        m.removeExpense(rawE.getId());

        section("C  ExpenseManager – update non-existent id");
        assertFalse("C12 updateExpense returns false for missing id",
                m.updateExpense(Integer.MAX_VALUE, 10, "X", "x"));

        section("C  ExpenseManager – remove");
        m.addExpense(15, "RemCat", "rem");
        Expense rem = lastOf(m);
        int sizeBeforeRem = m.getAllExpenses().size();
        assertTrue("C13 removeExpense returns true",      m.removeExpense(rem.getId()));
        assertEqual("C14 size decreases after remove",
                String.valueOf(sizeBeforeRem - 1), String.valueOf(m.getAllExpenses().size()));
        assertFalse("C15 findById after remove returns null",
                m.findById(rem.getId()) != null);

        assertFalse("C16 removeExpense non-existent returns false",
                m.removeExpense(Integer.MAX_VALUE));
        assertFalse("C17 removeExpense id 0 returns false",
                m.removeExpense(0));
        assertFalse("C18 removeExpense negative id returns false",
                m.removeExpense(-1));

        section("C  ExpenseManager – getAllExpenses defensive copy");
        List<Expense> copy = m.getAllExpenses();
        int originalSize = copy.size();
        copy.clear();
        assertEqual("C19 clearing returned list does not affect manager",
                String.valueOf(originalSize), String.valueOf(m.getAllExpenses().size()));

        // ── D: ExpenseManager – queries ───────────────────────────────────────
        section("D  ExpenseManager – sorting");
        List<Expense> byAmt = m.getSortedExpenses("amount");
        assertTrue("D01 sort by amount: list not null", byAmt != null);
        for (int i = 1; i < byAmt.size(); i++) {
            if (byAmt.get(i).getAmount() < byAmt.get(i-1).getAmount()) {
                fail("D02 sort by amount ascending order broken at index " + i); break;
            }
        }
        pass("D02 sort by amount: ascending order verified");

        List<Expense> byDate = m.getSortedExpenses("date");
        for (int i = 1; i < byDate.size(); i++) {
            if (byDate.get(i).getDate().isBefore(byDate.get(i-1).getDate())) {
                fail("D03 sort by date ascending order broken at index " + i); break;
            }
        }
        pass("D03 sort by date: ascending order verified");

        List<Expense> byUnknown = m.getSortedExpenses("xyz");
        assertTrue("D04 unknown sort key falls back to date (non-null result)", byUnknown != null);
        assertEqual("D05 unknown sort key same size as original",
                String.valueOf(m.getAllExpenses().size()), String.valueOf(byUnknown.size()));

        section("D  ExpenseManager – month filter");
        // year 1901 should have no data
        List<Expense> farPast = m.getExpensesByMonth(1, 1901);
        assertEqual("D06 month filter far past returns empty", "0", String.valueOf(farPast.size()));

        // Add expense for a known past month/year and verify it appears
        m.addExpense(77, "MonthTest", "D07");
        // we can only test current month since date is LocalDate.now()
        LocalDate now = LocalDate.now();
        List<Expense> currMonth = m.getExpensesByMonth(now.getMonthValue(), now.getYear());
        assertTrue("D07 current-month filter finds newly added expense", currMonth.size() >= 1);
        assertTrue("D08 all results in correct month",
                currMonth.stream().allMatch(ex -> ex.getDate().getMonthValue() == now.getMonthValue()
                        && ex.getDate().getYear() == now.getYear()));
        m.removeExpense(lastOf(m).getId());

        // different year, same month — should not appear
        List<Expense> wrongYear = m.getExpensesByMonth(now.getMonthValue(), 1901);
        assertEqual("D09 same month, wrong year: empty", "0", String.valueOf(wrongYear.size()));

        section("D  ExpenseManager – category filter");
        m.addExpense(33, "UniqueCat99", "D10 test");
        List<Expense> exact   = m.getExpensesByCategory("UniqueCat99");
        List<Expense> upper   = m.getExpensesByCategory("UNIQUECAT99");
        List<Expense> lower   = m.getExpensesByCategory("uniquecat99");
        List<Expense> missing = m.getExpensesByCategory("NoSuchCategory_XYZ");
        assertTrue("D10 category exact match",          exact.size() >= 1);
        assertTrue("D11 category upper-case match",     upper.size() >= 1);
        assertTrue("D12 category lower-case match",     lower.size() >= 1);
        assertEqual("D13 no-match category returns empty", "0", String.valueOf(missing.size()));
        m.removeExpense(lastOf(m).getId());

        section("D  ExpenseManager – totals");
        // calculateTotalExpenses should equal manual sum
        List<Expense> all = m.getAllExpenses();
        double manualSum = 0;
        for (Expense ex : all) manualSum += ex.getAmount();
        assertTrue("D14 calculateTotalExpenses matches manual sum",
                Math.abs(m.calculateTotalExpenses() - manualSum) < 0.001);

        // calculateMonthlyTotal for far past = 0
        assertTrue("D15 calculateMonthlyTotal far past = 0.0",
                m.calculateMonthlyTotal(1, 1901) == 0.0);

        // add known amount and verify monthly total includes it
        m.addExpense(444.44, "TotalTest", "D16");
        double totalBefore = m.calculateMonthlyTotal(now.getMonthValue(), now.getYear());
        assertTrue("D16 monthly total includes new expense", totalBefore >= 444.44);
        m.removeExpense(lastOf(m).getId());

        // ── E: Budget ─────────────────────────────────────────────────────────
        section("E  Budget");
        double savedBudget = m.getMonthlyBudget();

        m.setMonthlyBudget(50000.0);
        assertTrue("E01 setMonthlyBudget takes effect immediately", m.getMonthlyBudget() == 50000.0);

        // reload — persisted?
        ExpenseManager m2 = new ExpenseManager();
        assertTrue("E02 budget persists across reload", m2.getMonthlyBudget() == 50000.0);

        // remaining = budget - currentMonthSpend
        double spent = m.calculateMonthlyTotal(now.getMonthValue(), now.getYear());
        double expectedRemaining = 50000.0 - spent;
        assertTrue("E03 getRemainingBudget = budget - monthlySpend",
                Math.abs(m.getRemainingBudget() - expectedRemaining) < 0.001);

        // very large budget → remaining positive
        m.setMonthlyBudget(999_999_999.0);
        assertTrue("E04 remaining positive with huge budget", m.getRemainingBudget() >= 0);

        // tiny budget → remaining negative after adding expense
        m.setMonthlyBudget(0.01);
        m.addExpense(1000, "OverBudget", "E05");
        assertTrue("E05 remaining negative when over budget", m.getRemainingBudget() < 0);
        m.removeExpense(lastOf(m).getId());

        m.setMonthlyBudget(savedBudget);  // restore

        // ── F: Persistence round-trip ──────────────────────────────────────────
        section("F  Persistence round-trips");
        m.addExpense(321.0, "PersistCat", "F01 persist test");
        int persistId = lastOf(m).getId();

        ExpenseManager m3 = new ExpenseManager();
        assertTrue("F01 added expense found after reload", m3.findById(persistId) != null);
        if (m3.findById(persistId) != null) {
            assertEqual("F02 reloaded amount correct", "321.0",
                    String.valueOf(m3.findById(persistId).getAmount()));
            assertEqual("F03 reloaded category correct", "PersistCat",
                    m3.findById(persistId).getCategory());
        }

        // update → reload → verify
        m.updateExpense(persistId, 999, "UpdatedCat", "F04 updated");
        ExpenseManager m4 = new ExpenseManager();
        Expense reloaded = m4.findById(persistId);
        assertTrue("F04 updated expense found after reload", reloaded != null);
        if (reloaded != null) {
            assertEqual("F05 reloaded updated amount",   "999.0",      String.valueOf(reloaded.getAmount()));
            assertEqual("F06 reloaded updated category", "UpdatedCat", reloaded.getCategory());
        }

        // delete → reload → verify gone
        m.removeExpense(persistId);
        ExpenseManager m5 = new ExpenseManager();
        assertFalse("F07 deleted expense not found after reload",
                m5.findById(persistId) != null);

        // ── G: Edge cases ──────────────────────────────────────────────────────
        section("G  Edge cases");

        // Very small amount
        m.addExpense(0.01, "Tiny", "smallest possible");
        Expense tiny = lastOf(m);
        assertTrue("G01 very small amount (0.01) stored correctly", tiny.getAmount() == 0.01);
        m.removeExpense(tiny.getId());

        // Very large amount
        m.addExpense(1_000_000_000.0, "Huge", "large amount");
        Expense huge = lastOf(m);
        assertTrue("G02 very large amount stored correctly", huge.getAmount() == 1_000_000_000.0);
        m.removeExpense(huge.getId());

        // Category with special characters
        m.addExpense(10, "Café & Snacks", "special chars");
        Expense special = lastOf(m);
        assertEqual("G03 special chars in category preserved",
                "Café & Snacks", special.getCategory());
        m.removeExpense(special.getId());

        // Long description
        String longDesc = "This is a very long description that contains many words and should be stored and retrieved correctly";
        m.addExpense(10, "LongDesc", longDesc);
        Expense longE = lastOf(m);
        assertEqual("G04 long description stored correctly", longDesc, longE.getDescription());
        m.removeExpense(longE.getId());

        // Multiple spaces in category collapsed
        m.addExpense(10, "a   b   c", "G05");
        Expense spaceE = lastOf(m);
        assertEqual("G05 multiple internal spaces collapsed", "a b c", spaceE.getCategory());
        m.removeExpense(spaceE.getId());

        // Category filter finds after sanitization
        m.addExpense(50, "  Wellness  ", "G06");
        Expense wellnessE = lastOf(m);
        List<Expense> wellnessSearch = m.getExpensesByCategory("Wellness");
        assertTrue("G06 category filter works after sanitized storage", wellnessSearch.size() >= 1);
        m.removeExpense(wellnessE.getId());

        // getSortedExpenses on empty list (after removing everything)
        ExpenseManager empty = new ExpenseManager();
        // We won't clear the real file; just verify sort on current list handles 0/1 items gracefully
        assertTrue("G07 getSortedExpenses returns non-null always",
                empty.getSortedExpenses("amount") != null);
        assertTrue("G08 getExpensesByMonth returns non-null always",
                empty.getExpensesByMonth(1, 1901) != null);
        assertTrue("G09 getExpensesByCategory returns non-null always",
                empty.getExpensesByCategory("nope") != null);

        // Validator: extreme whitespace
        assertEqual("G10 sanitize only whitespace → empty", "", Validator.sanitize("         "));
        assertEqual("G11 sanitize mixed whitespace", "a b", Validator.sanitize(" a  \t b "));

        // fromCSVString: spaces around values are trimmed
        Expense spaceCSV = Expense.fromCSVString(" 1 , 100.0 , Food , 2026-01-01 , Dinner ");
        assertTrue("G12 fromCSVString trims spaces around fields", spaceCSV != null);

        // Expense with amount having decimal precision
        Expense precise = new Expense(1, 123.456789, "Prec", today, "precision");
        assertTrue("G13 high-precision amount stored as-is", precise.getAmount() == 123.456789);

        // ── Results ────────────────────────────────────────────────────────────
        System.out.println("\n=== Results: " + passed + " passed, " + failed + " failed ===");
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static Expense lastOf(ExpenseManager m) {
        List<Expense> all = m.getAllExpenses();
        return all.isEmpty() ? null : all.get(all.size() - 1);
    }

    private static void section(String name) {
        System.out.println("\n[ " + name + " ]");
    }

    private static void assertTrue(String desc, boolean cond) {
        if (cond) pass(desc); else fail(desc + " [expected true]");
    }

    private static void assertFalse(String desc, boolean cond) {
        if (!cond) pass(desc); else fail(desc + " [expected false]");
    }

    private static void assertEqual(String desc, String expected, String actual) {
        if (expected.equals(actual)) pass(desc);
        else fail(desc + " [expected='" + expected + "', got='" + actual + "']");
    }

    private static void pass(String desc) {
        System.out.println("  PASS: " + desc);
        passed++;
    }

    private static void fail(String desc) {
        System.out.println("  FAIL: " + desc);
        failed++;
    }
}
