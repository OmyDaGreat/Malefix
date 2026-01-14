package xyz.malefic.frc.pingu.log

import edu.wpi.first.units.Measure
import edu.wpi.first.util.WPISerializable
import edu.wpi.first.util.struct.StructSerializable
import org.littletonrobotics.junction.Logger.recordOutput

/**
 * Top-level logging helper extension.
 *
 * Provides an infix extension method `String.log` that records values of many
 * common types (primitives, arrays, WPISerializable/StructSerializable,
 * and wpilib `Measure`) to `org.littletonrobotics.junction.Logger.recordOutput`.
 *
 * Unsupported types are converted to their string representation before
 * recording. Use it directly as: `"myKey" log value`
 */
infix fun String.log(value: Any) {
    when (value) {
        is Boolean -> {
            recordOutput(this, value)
        }

        is Int -> {
            recordOutput(this, value)
        }

        is Long -> {
            recordOutput(this, value)
        }

        is Float -> {
            recordOutput(this, value)
        }

        is Double -> {
            recordOutput(this, value)
        }

        is ByteArray -> {
            recordOutput(this, value)
        }

        is BooleanArray -> {
            recordOutput(this, value)
        }

        is IntArray -> {
            recordOutput(this, value.map { it.toLong() }.toLongArray())
        }

        is LongArray -> {
            recordOutput(this, value)
        }

        is FloatArray -> {
            recordOutput(this, value)
        }

        is DoubleArray -> {
            recordOutput(this, value)
        }

        is StructSerializable -> {
            recordOutput(this, value)
        }

        is WPISerializable -> {
            recordOutput(this, value)
        }

        is Measure<*> -> {
            recordOutput(this, value)
        }

        is Array<*> -> {
            @Suppress("UNCHECKED_CAST")
            when {
                value.isArrayOf<ByteArray>() -> recordOutput(this, value as Array<ByteArray>)
                value.isArrayOf<BooleanArray>() -> recordOutput(this, value as Array<BooleanArray>)
                value.isArrayOf<IntArray>() -> recordOutput(this, value as Array<IntArray>)
                value.isArrayOf<LongArray>() -> recordOutput(this, value as Array<LongArray>)
                value.isArrayOf<FloatArray>() -> recordOutput(this, value as Array<FloatArray>)
                value.isArrayOf<DoubleArray>() -> recordOutput(this, value as Array<DoubleArray>)
                value.isArrayOf<Array<String>>() -> recordOutput(this, value as Array<Array<String>>)
                value.isArrayOf<Array<StructSerializable>>() -> recordOutput(this, value as Array<Array<StructSerializable>>)
                else -> recordOutput(this, value.map { it.toString() }.toTypedArray())
            }
        }

        else -> {
            recordOutput(this, value.toString())
        }
    }
}
