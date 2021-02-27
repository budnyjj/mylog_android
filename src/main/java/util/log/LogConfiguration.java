package util.log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;

/**
 * Stores values for logging subsystem configuration.
 *
 * @author Raman Budny
 */
public final class LogConfiguration {

    @SuppressWarnings("CharsetObjectCanBeUsed")
    private static final Charset UTF_8 = Charset.forName("UTF-8");

    @NonNull
    public final String processTag;

    @Nullable
    @SuppressWarnings("PMD.DefaultPackage")
    /* default */ final PrintWriter writer;

    /**
     * TODO: update documentation.
     *
     * @param optionalSink optional output sink to write logs into
     */
    public LogConfiguration(@NonNull String processTag, @Nullable OutputStream optionalSink) {
        this.processTag = processTag;
        if (optionalSink == null) {
            writer = null;
        } else {
            writer = new PrintWriter(new OutputStreamWriter(optionalSink, UTF_8), true);
        }
    }

    @Override
    public String toString() {
        return "LogConfiguration{"
                + "processTag='" + processTag + '\''
                + '}';
    }
}
