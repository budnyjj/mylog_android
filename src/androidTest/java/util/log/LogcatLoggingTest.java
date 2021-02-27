package util.log;

import android.util.Log;

import androidx.test.filters.LargeTest;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import static util.log.TestUtils.NUM_CPU_CORES;
import static util.log.TestUtils.NUM_REPS_PERFORMANCE;
import static util.log.TestUtils.PROCESS_TAG;

/** Checks correctness and performance of logcat-only logging. */
@LargeTest
public class LogcatLoggingTest {
    private static final Logger L = Logging.getLogger(LogcatLoggingTest.class);

    private static final double PERF_SLOWDOWN_FRACTION = 1;

    @BeforeClass
    public static void setUp() {
        Logging.init(new LogConfiguration(PROCESS_TAG, null));
    }

    @Test
    public void singleThreadPerformance() {
        final long startBaselineLogTimeMs = System.currentTimeMillis();
        for (int i = 0; i < NUM_REPS_PERFORMANCE; ++i) {
            Log.d(PROCESS_TAG, "singleThreadPerformance: " + i);
        }
        final long baselineLogDurationMs = System.currentTimeMillis() - startBaselineLogTimeMs;
        for (int i = 0; i < NUM_REPS_PERFORMANCE; ++i) {
            L.d("singleThreadPerformance: " + i);
        }
        final long myLogDurationMs = System.currentTimeMillis() - startBaselineLogTimeMs - baselineLogDurationMs;
        final long maxLogDurationMs = (long) (baselineLogDurationMs * PERF_SLOWDOWN_FRACTION);
        Assert.assertTrue("My logger is much slower then raw Android one: "
                        + myLogDurationMs + " >> " + baselineLogDurationMs + " (ms)",
                myLogDurationMs <= maxLogDurationMs);
    }

    @Test
    public void multiThreadPerformance() {
        final int numLoggers = NUM_CPU_CORES;
        final int numRepetitionsPerLogger = NUM_REPS_PERFORMANCE / numLoggers;
        final long startBaselineLogTimeMs = System.currentTimeMillis();
        TestUtils.executeParallel(() -> {
            for (int i = 0; i < numRepetitionsPerLogger; ++i) {
                Log.d(PROCESS_TAG, "multiThreadPerformance: " + i);
            }
        }, numLoggers);
        final long baselineLogDurationMs = System.currentTimeMillis() - startBaselineLogTimeMs;
        TestUtils.executeParallel(() -> {
            for (int i = 0; i < numRepetitionsPerLogger; ++i) {
                L.d("multiThreadPerformance: " + i);
            }
        }, numLoggers);
        final long myLogDurationMs = System.currentTimeMillis() - startBaselineLogTimeMs - baselineLogDurationMs;
        final long maxLogDurationMs = (long) (baselineLogDurationMs * PERF_SLOWDOWN_FRACTION);
        Assert.assertTrue("My logger is much slower then raw Android one: "
                        + myLogDurationMs + " >> " + baselineLogDurationMs + " (ms)",
                myLogDurationMs <= maxLogDurationMs);
    }
}
