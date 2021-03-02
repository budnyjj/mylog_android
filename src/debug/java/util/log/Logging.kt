package util.log

import androidx.annotation.GuardedBy

/**
 * Logging subsystem implementation suitable for apps running in debug mode.
 *
 * @author Raman Budny
 */
object Logging {

    @GuardedBy("this")
    private val LOGGERS = HashMap<String, NativeLogger>(128)

    /**
     * Initializes logging subsystem.
     *
     * This method should be invoked before the first [Logger] public method invocation.
     *
     * @param configuration configuration parameters
     * @throws IllegalStateException if this method was invoked twice in the same process
     */
    fun init(configuration: LogConfiguration) {
        JavaLogger.init(configuration)
        NativeLogger.init(configuration)
    }

    /** Provides logger with tag derived from specified target class.  */
    fun getLogger(targetClass: Class<*>) = getLogger(targetClass.simpleName)

    /**
     * Provides logger with specified tag.
     *
     * Cached previously created loggers.
     */
    fun getLogger(classTag: String): NativeLogger {
        val bracedClassTag = "[$classTag] "
        return synchronized(this) {
            LOGGERS[bracedClassTag] ?: NativeLogger(bracedClassTag).apply { LOGGERS[bracedClassTag] = this }
        }
    }
}
