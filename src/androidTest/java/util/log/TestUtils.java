package util.log;

import androidx.annotation.NonNull;

import java.util.concurrent.CountDownLatch;

final class TestUtils {
    public static final int NUM_CPU_CORES = Runtime.getRuntime().availableProcessors();

    public static final String PROCESS_TAG = "MyLog";

    /** Number of repetitions in verification tests. */
    public static final int NUM_REPS_VERIFICATION = 10;

    /** Number of repetitions in performance tests. */
    public static final int NUM_REPS_PERFORMANCE = 500_000;

    public static final String MSG_PART_STRING = "{STRING PART}";

    private TestUtils() {
        // forbid construction
    }

    /** Executes specified runnable simultaneously on specified number of threads. */
    public static void executeParallel(@NonNull Runnable task, int numThreads) {
        final CountDownLatch doneSignal = new CountDownLatch(numThreads);
        for (int i = 0; i < numThreads; ++i) {
            new Thread() {

                @Override
                public void run() {
                    task.run();
                    doneSignal.countDown();
                }
            }.start();
        }
        try {
            doneSignal.await();
        } catch (InterruptedException ignored) {
            // ignore
        }
    }
}
