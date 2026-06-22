package sorter;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

/**
 * ParallelOddEvenMergesort
 * ═════════════════════════
 * Parallel implementation of Batcher's Odd-Even Merge Sort (1968)
 * using Java's ForkJoinPool / RecursiveAction framework.
 *
 * ──────────────────────────────────────────────────────────────────────
 * PSEUDO-CODE
 * ──────────────────────────────────────────────────────────────────────
 *
 * OddEvenMergesort(A, lo, hi):
 *   comparisons++                        ← pretest: (hi - lo > 1)?
 *   if hi - lo <= 1: return
 *   mid = (lo + hi) / 2
 *   FORK  OddEvenMergesort(A, lo,  mid) ← ForkJoinPool worker thread
 *   CALL  OddEvenMergesort(A, mid, hi)  ← current thread (concurrent)
 *   JOIN  forked task                   ← synchronise
 *   OddEvenMerge(A, lo, hi, step=1)
 *
 * OddEvenMerge(A, lo, hi, step):
 *   comparisons++                        ← pretest check
 *   if step*2 >= hi - lo:
 *     CompareExchange(A, lo, lo+step)
 *     return
 *   OddEvenMerge(A, lo,       hi, step*2)
 *   OddEvenMerge(A, lo+step,  hi, step*2)
 *   // Stitching loop — each iteration: 1 pretest + 1 gate
 *   i = lo+step
 *   while true:
 *     comparisons++                      ← pretest i+step < hi
 *     if i+step >= hi: break
 *     CompareExchange(A, i, i+step)     ← 1 cmp + possible 3 moves
 *     i += step*2
 *   (final failing pretest already counted above)
 *
 * CompareExchange(A, i, j):
 *   comparisons++
 *   if A[i] > A[j]: swap; moves += 3
 *
 * Padding: non-power-of-2 n padded to next power of 2 with Integer.MAX_VALUE.
 *
 * Complexity:
 *   Comparisons : O(n log n)
 *   Parallel depth : O(log n)
 * ──────────────────────────────────────────────────────────────────────
 */
public class ParallelOddEvenMergesort {

    // Below this size, skip fork() overhead and sort sequentially
    private static final int SEQUENTIAL_THRESHOLD = 512;

    // Shared pool — uses all available CPU cores
    private static final ForkJoinPool POOL = new ForkJoinPool();

    // ══════════════════════════════════════════════════════════════════════
    // Public entry point
    // ══════════════════════════════════════════════════════════════════════

    public static int[] sort(int[] input, OperationCounter counter) {
        final int n   = input.length;
        final int n2  = nextPow2(n);
        final int pad = n2 - n;

        // Build padded working array
        int[] data = new int[n2];
        System.arraycopy(input, 0, data, 0, n);
        if (pad > 0) {
            counter.addComparisons(1);   // branch: pad needed?
            counter.addMoves(pad);       // placing Integer.MAX_VALUE sentinels
            java.util.Arrays.fill(data, n, n2, Integer.MAX_VALUE);
        }

        // Launch parallel divide-and-conquer
        POOL.invoke(new MergesortTask(data, 0, n2, counter));

        // Strip sentinels
        if (pad > 0) {
            int[] result = new int[n];
            System.arraycopy(data, 0, result, 0, n);
            return result;
        }
        return data;
    }

    // ══════════════════════════════════════════════════════════════════════
    // RecursiveAction — fork/join parallelism
    // ══════════════════════════════════════════════════════════════════════

    private static final class MergesortTask extends RecursiveAction {

        private final int[]            data;
        private final int              lo, hi;
        private final OperationCounter counter;

        MergesortTask(int[] data, int lo, int hi, OperationCounter ctr) {
            this.data    = data;
            this.lo      = lo;
            this.hi      = hi;
            this.counter = ctr;
        }

        @Override
        protected void compute() {
            counter.incrementComparisons();            // pretest: hi-lo > 1?
            if (hi - lo <= 1) return;

            if (hi - lo <= SEQUENTIAL_THRESHOLD) {
                sequentialSort(data, lo, hi, counter);
                return;
            }

            int mid = (lo + hi) >>> 1;

            MergesortTask leftTask = new MergesortTask(data, lo,  mid, counter);
            leftTask.fork();                           // ← REAL PARALLELISM
            // Right half runs on this thread while left runs on pool thread
            new MergesortTask(data, mid, hi, counter).compute();
            leftTask.join();                           // synchronise

            oddEvenMerge(data, lo, hi, 1, counter);
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // Sequential fallback (below SEQUENTIAL_THRESHOLD)
    // ══════════════════════════════════════════════════════════════════════

    private static void sequentialSort(int[] data, int lo, int hi,
                                       OperationCounter counter) {
        counter.incrementComparisons();                // pretest: hi-lo > 1?
        if (hi - lo <= 1) return;

        int mid = (lo + hi) >>> 1;
        sequentialSort(data, lo,  mid, counter);
        sequentialSort(data, mid, hi,  counter);
        oddEvenMerge(data, lo, hi, 1, counter);
    }

    // ══════════════════════════════════════════════════════════════════════
    // Batcher's Odd-Even Merge
    // ══════════════════════════════════════════════════════════════════════

    static void oddEvenMerge(int[] data, int lo, int hi, int step,
                              OperationCounter counter) {
        int doubleStep = step << 1;

        counter.incrementComparisons();               // pretest: base case?
        if (doubleStep >= hi - lo) {
            compareExchange(data, lo, lo + step, counter);
            return;
        }

        oddEvenMerge(data, lo,        hi, doubleStep, counter);
        oddEvenMerge(data, lo + step, hi, doubleStep, counter);

        // Stitching loop: pretest-style — count the condition check every
        // iteration, including the final failing one.
        int i = lo + step;
        while (true) {
            counter.incrementComparisons();           // pretest: i+step < hi
            if (i + step >= hi) break;
            compareExchange(data, i, i + step, counter);
            i += doubleStep;
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // Compare-Exchange gate
    // ══════════════════════════════════════════════════════════════════════

    static void compareExchange(int[] data, int i, int j,
                                OperationCounter counter) {
        counter.incrementComparisons();               // gate comparison
        if (data[i] > data[j]) {
            counter.incrementMoves();                 // 3 moves: tmp, [i], [j]
            int tmp = data[i];
            data[i] = data[j];
            data[j] = tmp;
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // Helpers
    // ══════════════════════════════════════════════════════════════════════

    static int nextPow2(int n) {
        int p = 1;
        while (p < n) p <<= 1;
        return p;
    }
}
