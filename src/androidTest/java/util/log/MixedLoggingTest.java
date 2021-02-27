package util.log;

import android.content.Context;

import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static util.log.TestUtils.NUM_REPS_VERIFICATION;
import static util.log.TestUtils.PROCESS_TAG;

/** Checks correctness and performance of mixed (Logcat and file) logging. */
@LargeTest
public class MixedLoggingTest {
    private static final Logger L = Logging.getLogger(MixedLoggingTest.class);

    private static final String OUTPUT_FILENAME = "test_mixed.log";
    private static String sOutputPath;

    @BeforeClass
    public static void setUp() throws IOException {
        final Context context = InstrumentationRegistry.getInstrumentation().getContext();
        final File outputFile = new File(context.getExternalFilesDir(null), OUTPUT_FILENAME);
        sOutputPath = outputFile.getAbsolutePath();
        final LogConfiguration configuration = new LogConfiguration(PROCESS_TAG, new FileOutputStream(outputFile));
        Logging.init(configuration);
    }

    @Test
    public void verifyMessagesSingleThread() {
        L.i("verifyMessagesSingleThread: started: " + sOutputPath);
        for (int i = 0; i < NUM_REPS_VERIFICATION; ++i) {
            L.d("verifyMessagesSingleThread: " + i);
        }
        L.i("verifyMessagesSingleThread: completed");
    }
}
