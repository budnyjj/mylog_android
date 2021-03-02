package util.log

import android.os.ParcelFileDescriptor

/**
 * Stores values for logging subsystem configuration.
 *
 * @author Raman Budny
 */
data class LogConfiguration(
    val processTag: String
) {
    /** Points to file where logs should be written additionally to LogCat. */
    var logFileDescriptor: ParcelFileDescriptor? = null
}
