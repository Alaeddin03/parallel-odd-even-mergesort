package sorter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Main — driver for Parallel Odd-Even Mergesort
 * ══════════════════════════════════════════════
 *
 * Execution plan
 * ──────────────
 * For each dataset size [1000, 5000, 10000]:
 *   Run 1 — Random / original order
 *   Run 2 — Pre-sorted order  (best-case stress test)
 *
 * Outputs
 *   report.txt   — operation count tabulation + 10×10 matrices
 *   dataset.bin  — binary representation of the text dataset
 *
 * Run:
 *   javac -d out src/main/java/sorter/*.java
 *   java  -cp out sorter.Main
 */
public class Main {

    private static final int[] SIZES   = {1000, 5000, 10000};
    private static final String TXT    = "dataset.txt";
    private static final String BIN    = "dataset.bin";
    private static final String REPORT = "report.txt";

    public static void main(String[] args) throws Exception {
        System.out.println("Parallel Odd-Even Mergesort  (Java ForkJoinPool)");
        System.out.println("=".repeat(55));
        System.out.printf("Available processors : %d%n",
                          Runtime.getRuntime().availableProcessors());

        // ── 1. Load dataset ──────────────────────────────────────────────
        System.out.println("\n[1] Loading dataset …");
        int[] fullData = BinaryIO.loadDataset(TXT, BIN);
        System.out.printf("    Loaded %,d elements%n", fullData.length);

        List<RunResult> results = new ArrayList<>();
        Random rng = new Random(42);   // fixed seed → reproducible shuffle

        for (int size : SIZES) {
            System.out.printf("%n[Size = %,d]%n", size);

            int[] slice = Arrays.copyOf(fullData, size);

            // ── Fisher-Yates shuffle ─────────────────────────────────────
            int[] randomData = shuffled(slice, rng);

            // ── Run 1: Random / original order ───────────────────────────
            System.out.print("  Run 1 — Random order … ");
            RunResult r1 = runSort("Random  | n=" + size, randomData);
            results.add(r1);
            System.out.printf("cmp=%,d  moves=%,d  time=%.2f ms%n",
                              r1.comparisons, r1.moves, r1.elapsedMs);

            // ── Run 2: Pre-sorted order ──────────────────────────────────
            System.out.print("  Run 2 — Pre-sorted order … ");
            int[] sortedInput = randomData.clone();
            Arrays.sort(sortedInput);
            RunResult r2 = runSort("Sorted  | n=" + size, sortedInput);
            results.add(r2);
            System.out.printf("cmp=%,d  moves=%,d  time=%.2f ms%n",
                              r2.comparisons, r2.moves, r2.elapsedMs);
        }

        // ── Write report ─────────────────────────────────────────────────
        String report = ReportWriter.buildReport(results);
        ReportWriter.writeReport(REPORT, report);
        System.out.println("\n[✓] Report written → " + REPORT);
        System.out.println();
        System.out.println(report);
    }

    // ── Execute one sort run ─────────────────────────────────────────────

    private static RunResult runSort(String label, int[] data) {
        int[] unsortedSnap = Arrays.copyOf(data, Math.min(100, data.length));

        OperationCounter counter = new OperationCounter();
        long t0      = System.nanoTime();
        int[] sorted = ParallelOddEvenMergesort.sort(data, counter);
        long t1      = System.nanoTime();

        double elapsedMs     = (t1 - t0) / 1_000_000.0;
        int[]  sortedSnap    = Arrays.copyOf(sorted, Math.min(100, sorted.length));

        return new RunResult(label, data.length,
                             counter.getComparisons(), counter.getMoves(),
                             elapsedMs, unsortedSnap, sortedSnap);
    }

    // ── Fisher-Yates shuffle ─────────────────────────────────────────────

    private static int[] shuffled(int[] arr, Random rng) {
        int[] a = arr.clone();
        for (int i = a.length - 1; i > 0; i--) {
            int j   = rng.nextInt(i + 1);
            int tmp = a[i];
            a[i]    = a[j];
            a[j]    = tmp;
        }
        return a;
    }
}
