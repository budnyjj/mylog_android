package util.log;

import androidx.annotation.NonNull;

/**
 * No-op logging subsystem implementation suitable for apps running in Release mode.
 *
 * NOTE: its API should be manually kept compatible with Logging class built for Debug mode.
 *
 * @author Raman Budny
 */
public final class Logging {

    private static final Logger L = new Logger() {

        @Override
        public void v(@NonNull String msg) {
            // no-op
        }

        @Override
        public void d(@NonNull String msg) {
            // no-op
        }

        @Override
        public void i(@NonNull String msg) {
            // no-op
        }

        @Override
        public void w(@NonNull String msg) {
            // no-op
        }

        @Override
        public void e(@NonNull String msg) {
            // no-op
        }

        @Override
        public void e(@NonNull String msg, @NonNull Throwable throwable) {
            // no-op
        }
    };

    private Logging() {
        // forbid object construction
    }

    public static void init(@NonNull LogConfiguration configuration) {
        if (configuration.writer != null) {
            throw new IllegalArgumentException("Additional output stream is not supported in Release mode");
        }
    }

    @NonNull
    public static Logger getLogger(Class<?> unused) {
        return L;
    }

    @NonNull
    public static Logger getLogger(String unused) {
        return L;
    }
}
