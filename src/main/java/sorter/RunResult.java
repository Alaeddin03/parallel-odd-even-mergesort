package sorter;

/**
 * ADT: RunResult
 * ──────────────
 * Immutable value object that captures everything produced by one
 * sort execution: label, size, operation counts, elapsed time,
 * and the first-100-element snapshots needed for the 10×10 matrices.
 */
public final class RunResult {

    public final String label;
    public final int    size;
    public final long   comparisons;
    public final long   moves;
    public final double elapsedMs;
    public final int[]  unsortedSnapshot;  // first 100 elements before sort
    public final int[]  sortedSnapshot;    // first 100 elements after  sort

    public RunResult(String label,
                     int    size,
                     long   comparisons,
                     long   moves,
                     double elapsedMs,
                     int[]  unsortedSnapshot,
                     int[]  sortedSnapshot) {
        this.label            = label;
        this.size             = size;
        this.comparisons      = comparisons;
        this.moves            = moves;
        this.elapsedMs        = elapsedMs;
        this.unsortedSnapshot = unsortedSnapshot.clone();
        this.sortedSnapshot   = sortedSnapshot.clone();
    }
}
