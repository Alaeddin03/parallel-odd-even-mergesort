package sorter;

import java.util.concurrent.atomic.AtomicLong;

/**
 * ADT: OperationCounter
 * ─────────────────────
 * Tracks comparisons and moves across parallel threads using
 * AtomicLong so concurrent ForkJoinPool workers don't race.
 *
 * Interface (public contract):
 *   incrementComparisons()   — record one comparison
 *   incrementMoves(n)        — record n moves
 *   getComparisons()         — total comparisons so far
 *   getMoves()               — total moves so far
 *   reset()                  — zero both counters
 */
public class OperationCounter {

    private final AtomicLong comparisons = new AtomicLong(0);
    private final AtomicLong moves       = new AtomicLong(0);

    // ── Mutators ──────────────────────────────
    public void incrementComparisons() {
        comparisons.incrementAndGet();
    }

    public void addComparisons(long n) {
        comparisons.addAndGet(n);
    }

    public void incrementMoves() {
        moves.addAndGet(3);   // one swap = 3 moves (tmp, a[i]=a[j], a[j]=tmp)
    }

    public void addMoves(long n) {
        moves.addAndGet(n);
    }

    // ── Accessors ─────────────────────────────
    public long getComparisons() { return comparisons.get(); }
    public long getMoves()       { return moves.get();       }

    // ── Reset ─────────────────────────────────
    public void reset() {
        comparisons.set(0);
        moves.set(0);
    }

    @Override
    public String toString() {
        return String.format("comparisons=%,d  moves=%,d",
                             comparisons.get(), moves.get());
    }
}
