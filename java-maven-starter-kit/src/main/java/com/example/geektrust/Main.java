import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

public class Main {

    private static int equity, debt, gold;
    private static int sipEquity, sipDebt, sipGold;
    private static Map<String, double[]> monthlyChangeRates = new LinkedHashMap<>();
    private static Map<String, int[]> monthlyBalances = new LinkedHashMap<>();
    private static double ratioEquity, ratioDebt, ratioGold;

    private static final List<String> MONTH_ORDER = Arrays.asList(
            "JANUARY", "FEBRUARY", "MARCH", "APRIL", "MAY", "JUNE",
            "JULY", "AUGUST", "SEPTEMBER", "OCTOBER", "NOVEMBER", "DECEMBER"
    );

    public static void main(String[] args) throws Exception {
        BufferedReader reader = new BufferedReader(new FileReader(args[0]));
        String line;

        while ((line = reader.readLine()) != null) {
            String[] input = line.split(" ");
            String command = input[0];

            switch (command) {
                case "ALLOCATE":
                    handleAllocate(input);
                    break;
                case "SIP":
                    handleSIP(input);
                    break;
                case "CHANGE":
                    handleMonthlyChange(input);
                    break;
                case "BALANCE":
                    calculatePortfolioValues();
                    printBalance(input[1]);
                    break;
                case "REBALANCE":
                    calculatePortfolioValues();
                    performRebalance();
                    break;
            }
        }
        reader.close();
    }

    private static void handleAllocate(String[] input) {
        equity = Integer.parseInt(input[1]);
        debt = Integer.parseInt(input[2]);
        gold = Integer.parseInt(input[3]);

        int totalInitial = equity + debt + gold;

        ratioEquity = (double) equity / totalInitial;
        ratioDebt = (double) debt / totalInitial;
        ratioGold = (double) gold / totalInitial;

        monthlyBalances.put("JANUARY", new int[]{equity, debt, gold});
    }

    private static void handleSIP(String[] input) {
        sipEquity = Integer.parseInt(input[1]);
        sipDebt = Integer.parseInt(input[2]);
        sipGold = Integer.parseInt(input[3]);
    }

    private static void handleMonthlyChange(String[] input) {
        String month = input[4];
        double eqChange = Double.parseDouble(input[1].replace("%", ""));
        double debtChange = Double.parseDouble(input[2].replace("%", ""));
        double goldChange = Double.parseDouble(input[3].replace("%", ""));
        monthlyChangeRates.put(month, new double[]{eqChange, debtChange, goldChange});
    }

    private static void calculatePortfolioValues() {
        int e = equity, d = debt, g = gold;

        for (String month : MONTH_ORDER) {
            if (!monthlyChangeRates.containsKey(month)) continue;

            if (!month.equals("JANUARY")) {
                e += sipEquity;
                d += sipDebt;
                g += sipGold;
            }

            double[] rates = monthlyChangeRates.get(month);

            e = (int) Math.floor(e * (1 + rates[0] / 100));
            d = (int) Math.floor(d * (1 + rates[1] / 100));
            g = (int) Math.floor(g * (1 + rates[2] / 100));

            monthlyBalances.put(month, new int[]{e, d, g});
        }

        int[] last = monthlyBalances.get(getLatestUpdatedMonth());
        equity = last[0];
        debt = last[1];
        gold = last[2];
    }

    private static void printBalance(String month) {
        int[] vals = monthlyBalances.get(month);
        System.out.println(vals[0] + " " + vals[1] + " " + vals[2]);
    }

    private static void performRebalance() {
        boolean canJune = monthlyBalances.containsKey("JUNE");
        boolean canDecember = monthlyBalances.containsKey("DECEMBER");

        if (!canJune && !canDecember) {
            System.out.println("CANNOT_REBALANCE");
            return;
        }

        String month = canDecember ? "DECEMBER" : "JUNE";
        int[] lastValues = monthlyBalances.get(month);

        int total = lastValues[0] + lastValues[1] + lastValues[2];

        int newEq = (int) Math.floor(total * ratioEquity);
        int newDebt = (int) Math.floor(total * ratioDebt);
        int newGold = (int) Math.floor(total * ratioGold);

        System.out.println(newEq + " " + newDebt + " " + newGold);
    }

    private static String getLatestUpdatedMonth() {
        String last = "JANUARY";
        for (String m : MONTH_ORDER) {
            if (monthlyBalances.containsKey(m)) {
                last = m;
            }
        }
        return last;
    }
}