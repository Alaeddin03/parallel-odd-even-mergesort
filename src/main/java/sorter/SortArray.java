package sorter;

/**
 * ADT: SortArray
 * ──────────────
 * Encapsulates the working array and all instrumented access primitives.
 * External code never touches the raw array — all comparisons and moves
 * go through this class so the OperationCounter stays accurate.
 *
 * Interface (public contract):
 *   get(i)                   — read element at index i
 *   compareExchange(i, j)    — Batcher gate: if data[i]>data[j] swap them
 *   size()                   — length of the array
 *   toArray()                — defensive copy of current data
 *   snapshot(from, len)      — defensive copy of a sub-range
 */
public class SortArray {

    private final int[]            data;
    private final OperationCounter counter;

    // ── Constructor ───────────────────────────
    public SortArray(int[] source, OperationCounter counter) {
        this.data    = source.clone();   // defensive copy — caller's array unchanged
        this.counter = counter;
    }

    // ── Accessor ──────────────────────────────
    public int get(int i) { return data[i]; }

    public int size() { return data.length; }

    // ── Core compare-exchange gate ─────────────
    /**
     * Compare-exchange: the fundamental building block of Batcher's network.
     * Counts 1 comparison; if a swap occurs, counts 3 moves.
     *
     * Pretest-loop style counting is applied in the sort/merge methods;
     * here we count the single gate evaluation.
     */
    public void compareExchange(int i, int j) {
        counter.incrementComparisons();          // the gate comparison
        if (data[i] > data[j]) {
            counter.incrementMoves();            // 3 moves: tmp, data[i], data[j]
            int tmp  = data[i];
            data[i]  = data[j];
            data[j]  = tmp;
        }
    }

    // ── Snapshot helpers ──────────────────────
    public int[] toArray() {
        return data.clone();
    }

    public int[] snapshot(int from, int len) {
        int[] out = new int[len];
        System.arraycopy(data, from, out, 0, len);
        return out;
    }
}
