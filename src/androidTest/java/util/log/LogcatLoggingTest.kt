package util.log

import android.util.Log
import androidx.test.filters.LargeTest
import org.junit.Assert
import org.junit.BeforeClass
import org.junit.Test

/** Checks correctness and performance of logcat-only logging.  */
@LargeTest
class LogcatLoggingTest {

    @Test
    fun singleThreadPerformance() {
        val startBaselineLogTimeMs = System.currentTimeMillis()
        for (i in 0 until TestUtils.NUM_REPS_PERFORMANCE) {
            Log.d(TestUtils.PROCESS_TAG, "singleThreadPerformance: $i")
        }
        val baselineLogDurationMs = System.currentTimeMillis() - startBaselineLogTimeMs
        for (i in 0 until TestUtils.NUM_REPS_PERFORMANCE) {
            L.d("singleThreadPerformance: $i")
        }
        val myLogDurationMs = System.currentTimeMillis() - startBaselineLogTimeMs - baselineLogDurationMs
        val maxLogDurationMs = (baselineLogDurationMs * PERF_SLOWDOWN_FRACTION).toLong()
        Assert.assertTrue(
            "My logger is much slower then raw Android one: $myLogDurationMs >> $baselineLogDurationMs (ms)",
            myLogDurationMs <= maxLogDurationMs)
    }

    @Test
    fun multiThreadPerformance() {
        val numLoggers = TestUtils.NUM_CPU_CORES
        val numRepetitionsPerLogger = TestUtils.NUM_REPS_PERFORMANCE / numLoggers
        val startBaselineLogTimeMs = System.currentTimeMillis()
        TestUtils.executeParallel({
            for (i in 0 until numRepetitionsPerLogger) {
                Log.d(TestUtils.PROCESS_TAG, "multiThreadPerformance: $i")
            }
        }, numLoggers)
        val baselineLogDurationMs = System.currentTimeMillis() - startBaselineLogTimeMs
        TestUtils.executeParallel({
            for (i in 0 until numRepetitionsPerLogger) {
                L.d("multiThreadPerformance: $i")
            }
        }, numLoggers)
        val myLogDurationMs = System.currentTimeMillis() - startBaselineLogTimeMs - baselineLogDurationMs
        val maxLogDurationMs = (baselineLogDurationMs * PERF_SLOWDOWN_FRACTION).toLong()
        Assert.assertTrue(
            "My logger is much slower then raw Android one: $myLogDurationMs >> $baselineLogDurationMs (ms)",
            myLogDurationMs <= maxLogDurationMs)
    }

    companion object {

        /** Allow my logger to be up to 5% slower in logcat-only mode.  */
        private const val PERF_SLOWDOWN_FRACTION = 0.8

        private val L = Logging.getLogger(LogcatLoggingTest::class.java)

        @BeforeClass
        @JvmStatic
        fun setUp() {
            Logging.init(LogConfiguration(TestUtils.PROCESS_TAG))
        }
    }
}