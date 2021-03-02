package util.log

/**
 * No-op logging subsystem implementation suitable for apps running in Release mode.
 *
 * NOTE: its API should be manually kept compatible with Logging class built for Debug mode.
 *
 * @author Raman Budny
 */
@Suppress("NOTHING_TO_INLINE")
object Logging {
    val L = Logger()

    fun init(configuration: LogConfiguration) {
        require(configuration.logFileDescriptor == null) {
            "Logging to a file is forbidden in Release mode"
        }
    }

    inline fun getLogger(unused: Class<*>?) = L

    inline fun getLogger(unused: String?) = L

    class Logger {

        inline fun v(msg: String) {}

        inline fun d(msg: String) {}

        inline fun i(msg: String) {}

        inline fun w(msg: String) {}

        inline fun e(msg: String) {}

        inline fun e(msg: String, throwable: Throwable) {}
    }
}