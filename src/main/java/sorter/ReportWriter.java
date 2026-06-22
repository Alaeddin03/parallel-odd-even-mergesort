package sorter;

import java.io.*;
import java.util.List;

/**
 * ReportWriter
 * ─────────────
 * Formats and writes the full execution report:
 *   - Operation count tabulation (all runs)
 *   - 10×10 matrix visual verification (first 100 elements, each run)
 */
public class ReportWriter {

    private ReportWriter() {}

    // ── 10×10 matrix ──────────────────────────────────────────────────────

    private static String matrix10x10(int[] arr) {
        StringBuilder sb = new StringBuilder();
        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 10; col++) {
                sb.append(String.format("%8d", arr[row * 10 + col]));
            }
            sb.append('\n');
        }
        return sb.toString();
    }

    // ── Table helpers ──────────────────────────────────────────────────────

    private static final String SEP =
        "+---------------------------------+---------+----------------+"
      + "------------+---------------+";

    private static final String HEADER =
        "| Run Label                       |    Size |    Comparisons |"
      + "      Moves |    Elapsed    |";

    private static String tableRow(RunResult r) {
        return String.format(
            "| %-31s | %7d | %14s | %10s | %11s ms |",
            r.label,
            r.size,
            String.format("%,d", r.comparisons),
            String.format("%,d", r.moves),
            String.format("%.3f", r.elapsedMs));
    }

    // ── Main report builder ───────────────────────────────────────────────

    public static String buildReport(List<RunResult> results) {
        StringBuilder sb = new StringBuilder();

        sb.append("=".repeat(80)).append('\n');
        sb.append(" PARALLEL ODD-EVEN MERGESORT — EXECUTION REPORT\n");
        sb.append(" Java " + System.getProperty("java.version")
                  + "  |  Cores: "
                  + Runtime.getRuntime().availableProcessors() + '\n');
        sb.append("=".repeat(80)).append("\n\n");

        // ── Tabulation ──
        sb.append("OPERATION COUNT TABULATION\n");
        sb.append(SEP).append('\n');
        sb.append(HEADER).append('\n');
        sb.append(SEP).append('\n');
        for (RunResult r : results) sb.append(tableRow(r)).append('\n');
        sb.append(SEP).append("\n\n");

        // ── 10×10 matrices ──
        sb.append("-".repeat(80)).append('\n');
        sb.append("10x10 MATRIX VISUAL VERIFICATION  (first 100 elements)\n");
        sb.append("-".repeat(80)).append('\n');

        for (RunResult r : results) {
            sb.append(String.format("\n[ %s ]\n", r.label));
            sb.append("\nUNSORTED (original order, first 100 elements):\n");
            sb.append(matrix10x10(r.unsortedSnapshot));
            sb.append("\nSORTED:\n");
            sb.append(matrix10x10(r.sortedSnapshot));
        }

        return sb.toString();
    }

    // ── Write to file ─────────────────────────────────────────────────────

    public static void writeReport(String path, String content)
            throws IOException {
        try (PrintWriter pw = new PrintWriter(
                new BufferedWriter(new FileWriter(path)))) {
            pw.print(content);
        }
    }
}
