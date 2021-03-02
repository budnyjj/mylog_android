package util.log

import java.util.concurrent.atomic.AtomicBoolean

@Suppress("NOTHING_TO_INLINE")
class NativeLogger internal constructor(bracedClassTag: String) {

    private val mNativeLoggerPtr: Long

    init {
        // TODO: check return value
        mNativeLoggerPtr = nativeCtor(bracedClassTag)
    }

    // fun v(msg: String) {}

    fun d(msg: String) = nativeD(mNativeLoggerPtr, msg)

    // fun i(msg: String) {}
    //
    // fun w(msg: String) {}
    //
    // fun e(msg: String) {}
    //
    // fun e(msg: String, throwable: Throwable) {}

    private external fun nativeCtor(bracedClassTag: String): Long

    private external fun nativeD(nativeLoggerPtr: Long, msg: String)

    companion object {

        /**
         * Stores the tags of the process which performs logging.
         *
         * This field is expected to stay non-null and unmodified after [.init] method invocation.
         */
        private val sInitialized = AtomicBoolean()

        internal fun init(configuration: LogConfiguration) {
            check(!sInitialized.getAndSet(true)) {
                "Native logger is already initialized. This method should be invoked once per process."
            }
            nativeInit(configuration.processTag, configuration.logFileDescriptor?.detachFd() ?: -1)
        }

        /**
         * Initializes native logger.
         *
         * @param processTag tag of the process to log
         * @param logFileDescriptor descriptor of additional file to log into or negative value if there is no one
         */
        @JvmStatic
        private external fun nativeInit(processTag: String, logFileDescriptor: Int)

        init {
            System.loadLibrary("mylog_android")
        }
    }
}