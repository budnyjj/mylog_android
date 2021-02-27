package util.log;

import android.util.Log;

import androidx.annotation.NonNull;

import java.io.PrintWriter;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Logging subsystem implementation suitable for apps running in debug mode.
 *
 * @author Raman Budny
 */
@SuppressWarnings("PMD.MoreThanOneLogger")
public final class Logging {
    private static final ConcurrentMap<String, Logger> LOGGERS = new ConcurrentHashMap<>(128);

    /**
     * Stores the tags of the process which performs logging.
     *
     * This field is expected to stay non-null and unmodified after {@link #init} method invocation.
     *
     * NOTE: this field is package-local so as to avoid redundant accessor method generation:
     * https://pmd.github.io/pmd-6.0.0/pmd_rules_java_bestpractices.html#accessormethodgeneration
     */
    @SuppressWarnings("PMD.DefaultPackage")
    /* default */ static volatile LogConfiguration sConfiguration;

    private Logging() {
        // forbid object construction
    }

    /**
     * Initializes logging subsystem.
     *
     * This method should be invoked before the first {@link Logger} public method invocation.
     *
     * @param configuration configuration parameters
     * @throws IllegalStateException if this method was invoked twice in the same process
     */
    public static void init(@NonNull LogConfiguration configuration) {
        if (sConfiguration != null) {
            throw new IllegalStateException("Logging process tag is already configured. "
                    + "It seems like this method is invoked in this process the second time");
        }
        sConfiguration = configuration;
    }

    /** Provides logger with tag derived from specified target class. */
    @NonNull
    public static Logger getLogger(@NonNull Class<?> targetClass) {
        return getLogger(targetClass.getSimpleName());
    }

    /**
     * Provides logger with specified tag.
     *
     * Cached previously created loggers.
     */
    @NonNull
    public static Logger getLogger(@NonNull String classTag) {
        final String bracedClassTag = '[' + classTag + "] ";
        final Logger newLogger = new JavaLogger(bracedClassTag);
        final Logger existingLogger = LOGGERS.putIfAbsent(bracedClassTag, newLogger);
        return existingLogger == null ? newLogger : existingLogger;
    }

    /**
     * Prints formatted messages to the Android LogCat.
     *
     * NOTE: We use {@link String#concat} to concatenate pairs of strings there cause it was
     * proven that it requires less CPU time than other concatenation utilities
     * (plus sign, explicit StreamBuilder and StreamBuffer objects).
     */
    @SuppressWarnings("ConstantConditions")
    public static final class JavaLogger implements Logger {

        /** Upper-bound estimate of the log message prefix length. */
        private static final int LENGTH_SYSTEM_INFO = 60;

        /** Length of log timestamp with the space at the end. */
        private static final int LENGTH_TIMESTAMP = 19;

        private static final int PROCESS_ID = android.os.Process.myPid();

        private static final ThreadLocal<ThreadLocals> THREAD_LOCALS = new ThreadLocal<ThreadLocals>() {

            @Override
            protected ThreadLocals initialValue() {
                return new ThreadLocals(new GregorianCalendar(Locale.US), android.os.Process.myTid());
            }
        };

        @NonNull
        private final String mBracedClassTag;

        /* default */ JavaLogger(@NonNull String bracedClassTag) {
            mBracedClassTag = bracedClassTag;
        }

        @Override
        public void v(@NonNull String msg) {
            final String logcatMsg = mBracedClassTag.concat(msg);
            final LogConfiguration configuration = sConfiguration;
            final String processTag = configuration.processTag;
            final PrintWriter writer = configuration.writer;
            if (writer == null) {
                Log.v(processTag, logcatMsg);
            } else {
                final StringBuilder writerMsgBuilder = new StringBuilder(LENGTH_SYSTEM_INFO + logcatMsg.length());
                final ThreadLocals threadLocals = THREAD_LOCALS.get();
                appendThreadInfo(writerMsgBuilder, threadLocals.threadId);
                writerMsgBuilder.append(" V ");
                appendMessage(writerMsgBuilder, processTag, logcatMsg);
                synchronized (Logging.class) {
                    Log.v(processTag, logcatMsg);
                    prependTimestamp(writerMsgBuilder, threadLocals.calendar);
                    writer.println(writerMsgBuilder);
                }
            }
        }

        @Override
        public void d(@NonNull String msg) {
            final String logcatMsg = mBracedClassTag.concat(msg);
            final LogConfiguration configuration = sConfiguration;
            final String processTag = configuration.processTag;
            final PrintWriter writer = configuration.writer;
            if (writer == null) {
                Log.d(processTag, logcatMsg);
            } else {
                final StringBuilder writerMsgBuilder = new StringBuilder(LENGTH_SYSTEM_INFO + logcatMsg.length());
                final ThreadLocals threadLocals = THREAD_LOCALS.get();
                appendThreadInfo(writerMsgBuilder, threadLocals.threadId);
                writerMsgBuilder.append(" D ");
                appendMessage(writerMsgBuilder, processTag, logcatMsg);
                synchronized (Logging.class) {
                    Log.d(processTag, logcatMsg);
                    prependTimestamp(writerMsgBuilder, threadLocals.calendar);
                    writer.println(writerMsgBuilder);
                }
            }
        }

        @Override
        public void i(@NonNull String msg) {
            final String logcatMsg = mBracedClassTag.concat(msg);
            final LogConfiguration configuration = sConfiguration;
            final String processTag = configuration.processTag;
            final PrintWriter writer = configuration.writer;
            if (writer == null) {
                Log.i(processTag, logcatMsg);
            } else {
                final StringBuilder writerMsgBuilder = new StringBuilder(LENGTH_SYSTEM_INFO + logcatMsg.length());
                final ThreadLocals threadLocals = THREAD_LOCALS.get();
                appendThreadInfo(writerMsgBuilder, threadLocals.threadId);
                writerMsgBuilder.append(" I ");
                appendMessage(writerMsgBuilder, processTag, logcatMsg);
                synchronized (Logging.class) {
                    Log.i(processTag, logcatMsg);
                    prependTimestamp(writerMsgBuilder, threadLocals.calendar);
                    writer.println(writerMsgBuilder);
                }
            }
        }

        @Override
        public void w(@NonNull String msg) {
            final String logcatMsg = mBracedClassTag.concat(msg);
            final LogConfiguration configuration = sConfiguration;
            final String processTag = configuration.processTag;
            final PrintWriter writer = configuration.writer;
            if (writer == null) {
                Log.w(processTag, logcatMsg);
            } else {
                final StringBuilder writerMsgBuilder = new StringBuilder(LENGTH_SYSTEM_INFO + logcatMsg.length());
                final ThreadLocals threadLocals = THREAD_LOCALS.get();
                appendThreadInfo(writerMsgBuilder, threadLocals.threadId);
                writerMsgBuilder.append(" W ");
                appendMessage(writerMsgBuilder, processTag, logcatMsg);
                synchronized (Logging.class) {
                    Log.w(processTag, logcatMsg);
                    prependTimestamp(writerMsgBuilder, threadLocals.calendar);
                    writer.println(writerMsgBuilder);
                }
            }
        }

        @Override
        public void e(@NonNull String msg) {
            final String logcatMsg = mBracedClassTag.concat(msg);
            final LogConfiguration configuration = sConfiguration;
            final String processTag = configuration.processTag;
            final PrintWriter writer = configuration.writer;
            if (writer == null) {
                Log.e(processTag, logcatMsg);
            } else {
                final StringBuilder writerMsgBuilder = new StringBuilder(LENGTH_SYSTEM_INFO + logcatMsg.length());
                final ThreadLocals threadLocals = THREAD_LOCALS.get();
                appendThreadInfo(writerMsgBuilder, threadLocals.threadId);
                writerMsgBuilder.append(" E ");
                appendMessage(writerMsgBuilder, processTag, logcatMsg);
                synchronized (Logging.class) {
                    Log.e(processTag, logcatMsg);
                    prependTimestamp(writerMsgBuilder, threadLocals.calendar);
                    writer.println(writerMsgBuilder);
                }
            }
        }

        @Override
        public void e(@NonNull String msg, @NonNull Throwable throwable) {
            final String logcatMsg = mBracedClassTag.concat(msg);
            final LogConfiguration configuration = sConfiguration;
            final String processTag = configuration.processTag;
            final PrintWriter writer = configuration.writer;
            if (writer == null) {
                Log.e(processTag, logcatMsg, throwable);
            } else {
                final String throwableMsg = Log.getStackTraceString(throwable);
                final StringBuilder writerMsgBuilder =
                        new StringBuilder(LENGTH_SYSTEM_INFO + logcatMsg.length() + throwableMsg.length());
                final ThreadLocals threadLocals = THREAD_LOCALS.get();
                appendThreadInfo(writerMsgBuilder, threadLocals.threadId);
                writerMsgBuilder.append(" E ");
                appendMessage(writerMsgBuilder, processTag, logcatMsg);
                synchronized (Logging.class) {
                    Log.e(processTag, logcatMsg);
                    prependTimestamp(writerMsgBuilder, threadLocals.calendar);
                    writer.println(writerMsgBuilder);
                    writer.println(throwableMsg);
                }
            }
        }

        private static void appendThreadInfo(@NonNull StringBuilder builder, int threadId) {
            builder.append(PROCESS_ID).append(' ').append(threadId);
        }

        private static void appendMessage(
                @NonNull StringBuilder builder,
                @NonNull String processTag,
                @NonNull String message
        ) {
            builder.append(processTag).append(": ").append(message);
        }

        private static void prependTimestamp(
                @NonNull StringBuilder msgBuilder,
                @NonNull Calendar calendar
        ) {
            calendar.setTimeInMillis(System.currentTimeMillis());
            final StringBuilder timestampBuilder = new StringBuilder(LENGTH_TIMESTAMP);
            final int month = calendar.get(Calendar.MONTH);
            if (month < 10) {
                timestampBuilder.append('0');
            }
            timestampBuilder.append(month).append('-');
            final int day = calendar.get(Calendar.DAY_OF_MONTH);
            if (day < 10) {
                timestampBuilder.append('0');
            }
            timestampBuilder.append(day).append(' ');
            final int hour = calendar.get(Calendar.HOUR);
            if (hour < 10) {
                timestampBuilder.append('0');
            }
            timestampBuilder.append(hour).append(':');
            final int minute = calendar.get(Calendar.MINUTE);
            if (minute < 10) {
                timestampBuilder.append('0');
            }
            timestampBuilder.append(minute).append(':');
            final int second = calendar.get(Calendar.SECOND);
            if (second < 10) {
                timestampBuilder.append('0');
            }
            timestampBuilder.append(second).append(':');
            final int millisecond = calendar.get(Calendar.MILLISECOND);
            if (millisecond < 10) {
                timestampBuilder.append("00");
            } else if (millisecond < 100) {
                timestampBuilder.append('0');
            }
            timestampBuilder.append(' ');
            msgBuilder.insert(0, timestampBuilder.toString());
        }

        /** Stores objects that should be accessed using ThreadLocal objects. */
        private static final class ThreadLocals {
            final Calendar calendar;
            final int threadId;

            ThreadLocals(@NonNull Calendar calendar, int threadId) {
                this.calendar = calendar;
                this.threadId = threadId;
            }
        }
    }
}
