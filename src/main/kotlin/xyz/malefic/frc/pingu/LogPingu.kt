package xyz.malefic.frc.pingu

import edu.wpi.first.util.WPISerializable
import edu.wpi.first.util.struct.StructSerializable
import org.littletonrobotics.junction.Logger.recordMetadata
import org.littletonrobotics.junction.Logger.recordOutput

/**
 * Type alias for a pair consisting of a log message and any associated data.
 */
typealias Log = Pair<String, Any>

/**
 * Singleton object for logging key-value pairs and metadata within the project.
 *
 * Provides utility functions to log various data types, manage ignored files,
 * and batch log entries. Integrates with WPILib and AdvantageKit
 * for output and metadata recording, primarily for test mode operations.
 */
object LogPingu {
    private val capturedLogs = mutableListOf<Log>()

    /**
     * Represents files to ignore during logging.
     * Can also include directories.
     * Files and folders are not required to have a file path,
     * will ignore all files and folders with the same name.
     * Ignored files will use project root as base path
     * Case-sensitive
     */
    val IGNORED_FILES = mutableSetOf<String>()

    /**
     * Indicates whether the system is in test mode.
     * When `true`, logging functions will record outputs.
     */
    @Suppress("ktlint:standard:property-naming")
    var TEST_MODE: Boolean = true

    /**
     * Adds a file or directory name to the set of ignored files for logging.
     *
     * @param name The name of the file or directory to ignore. Case-sensitive. Uses project root as base path.
     */
    fun addIgnoredFile(name: String) = IGNORED_FILES.add(name)

    /**
     * Logs a key-value pair.
     *
     * @param string The key associated with the value to log.
     * @param value The value to log.
     */
    @JvmStatic
    fun log(
        string: String,
        value: Any,
    ) {
        capturedLogs.add(Log(string, value))
    }

    /**
     * Logs a key-value pair.
     *
     * @receiver The key associated with the value to log.
     * @param value The value to log.
     */
    infix fun String.to(value: Any) {
        capturedLogs.add(Log(this, value))
    }

    /**
     * Logs a key-value pair.
     *
     * @receiver The key associated with the value to log.
     * @param value The value to log.
     */
    operator fun String.plus(value: Any) {
        capturedLogs.add(Log(this, value))
    }

    /**
     * Executes a block of code, capturing all logs generated within the block.
     *
     * This method clears the current list of captured logs, runs the provided block,
     * and then logs all captured key-value pairs at once. Useful for grouping related
     * log entries together for batch processing.
     *
     * @param block The block of code whose logs should be captured and processed.
     */
    @JvmStatic
    fun logs(block: Runnable) {
        capturedLogs.clear()

        block.run()

        logs(*capturedLogs.toTypedArray())
    }

    /**
     * Logs multiple values with their respective keys based on the type of each value.
     *
     * @param logs Vararg parameter of pairs where the first element is the key and the second element is the value to log.
     */
    @JvmStatic
    @SafeVarargs
    fun logs(vararg logs: Log) = logs.forEach { (key, value) -> logs(key, value) }

    /**
     * Logs a value of generic type [B] with a specified key if the system is in test mode,
     * unless the class name of [A] matches any entry in [IGNORED_FILES].
     *
     * @param key The key associated with the value to log.
     * @param value The value to log, of generic type [B].
     *
     * If the value is of type Double, Int, Boolean, String, or WPISerializable, it is logged directly.
     * Otherwise, the value is converted to a String before logging.
     * Logging is skipped if the class name of [A] contains any ignored file name.
     */
    @JvmStatic
    inline fun <reified A, reified B> A.logs(
        key: String?,
        value: B,
    ) {
        if (IGNORED_FILES.any { A::class.java.name.contains(it, true) }) return

        if (TEST_MODE) {
            when (value) {
                is Double -> recordOutput(key, value)
                is Int -> recordOutput(key, value)
                is Boolean -> recordOutput(key, value)
                is String -> recordOutput(key, value)
                is WPISerializable -> recordOutput(key, value)
                else -> recordOutput(key, "String: $value")
            }
        }
    }

    /**
     * Logs a [Double] value with a specified key if the system is in test mode.
     *
     * @param key The key associated with the value to log.
     * @param value The [Double] value to log.
     */
    @JvmStatic
    fun logs(
        key: String?,
        value: Double,
    ) {
        if (TEST_MODE) {
            recordOutput(key, value)
        }
    }

    /**
     * Logs an [Int] value with a specified key if the system is in test mode.
     *
     * @param key The key associated with the value to log.
     * @param value The [Int] value to log.
     */
    @JvmStatic
    fun logs(
        key: String?,
        value: Int,
    ) {
        if (TEST_MODE) {
            recordOutput(key, value)
        }
    }

    /**
     * Logs a [Boolean] value with a specified key if the system is in test mode.
     *
     * @param key The key associated with the value to log.
     * @param value The [Boolean] value to log.
     */
    @JvmStatic
    fun logs(
        key: String?,
        value: Boolean,
    ) {
        if (TEST_MODE) {
            recordOutput(key, value)
        }
    }

    /**
     * Logs a [String] value with a specified key if the system is in test mode.
     *
     * @param key The key associated with the value to log.
     * @param value The [String] value to log.
     */
    @JvmStatic
    fun logs(
        key: String?,
        value: String?,
    ) {
        if (TEST_MODE) {
            recordOutput(key, value)
        }
    }

    /**
     * Logs a [WPISerializable] value with a specified key if the system is in test mode.
     *
     * @param key The key associated with the value to log.
     * @param value The [WPISerializable] value to log.
     */
    @JvmStatic
    fun <T : WPISerializable?> logs(
        key: String?,
        value: T,
    ) {
        if (TEST_MODE) {
            recordOutput(key, value)
        }
    }

    /**
     * Logs a [StructSerializable] value with a specified key if the system is in test mode.
     *
     * @param key The key associated with the value to log.
     * @param value The [StructSerializable] value to log.
     */
    @JvmStatic
    fun <T : StructSerializable?> logs(
        key: String?,
        vararg value: T,
    ) {
        if (TEST_MODE) {
            recordOutput(key, *value)
        }
    }

    /**
     * Logs a metadata value with a specified key if the system is in test mode.
     *
     * @param key The key associated with the value to log.
     * @param value The string value to log.
     */
    @JvmStatic
    fun metaLogs(
        key: String?,
        value: String?,
    ) {
        if (TEST_MODE) {
            recordMetadata(key, value)
        }
    }
}
