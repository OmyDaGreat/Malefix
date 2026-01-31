package xyz.malefic.frc.extension

import edu.wpi.first.math.geometry.Rotation2d
import edu.wpi.first.units.Units
import edu.wpi.first.units.measure.Angle
import xyz.malefic.frc.pingu.encoder.Engu

/**
 * Creates an [Engu] encoder configured for swerve module use.
 *
 * ## Usage Example:
 * ```kotlin
 * val encoder = swerveEncoder(
 *     id = 1,
 *     offset = Rotation2d.fromDegrees(45.0)
 * )
 * ```
 *
 * @param id CAN ID of the CANcoder.
 * @param offset Magnet offset for the encoder.
 * @param clockwisePositive Whether clockwise should be positive (default: false).
 * @return Configured [Engu] encoder.
 */
fun swerveEncoder(
    id: Int,
    offset: Rotation2d = Rotation2d(),
    clockwisePositive: Boolean = false,
): Engu =
    Engu(id).apply {
        configureSwerve(offset, clockwisePositive)
    }

/**
 * Extension to configure an existing [Engu] for swerve use with a DSL.
 *
 * ## Usage Example:
 * ```kotlin
 * val encoder = Engu(1).configureSwerve {
 *     offset = Rotation2d.fromDegrees(45.0)
 *     clockwisePositive = false
 * }
 * ```
 */
fun Engu.configureSwerve(block: SwerveEncoderConfig.() -> Unit): Engu {
    val config = SwerveEncoderConfig().apply(block)
    configureSwerve(config.offset, config.clockwisePositive)
    return this
}

/**
 * Configuration builder for swerve encoder setup.
 */
data class SwerveEncoderConfig(
    var offset: Rotation2d = Rotation2d(),
    var clockwisePositive: Boolean = false,
)

/**
 * Extension to get the angle error between current position and target.
 *
 * Useful for debugging swerve module alignment.
 *
 * @param target Target angle.
 * @return Angle error as [Rotation2d].
 */
fun Engu.getAngleError(target: Rotation2d): Rotation2d = target - rotation

/**
 * Extension to check if encoder is within tolerance of target angle.
 *
 * @param target Target angle.
 * @param tolerance Tolerance as a measured angle (default: 2.0 degrees).
 * @return True if within tolerance.
 */
fun Engu.isAtAngle(
    target: Rotation2d,
    tolerance: Angle = Units.Degrees.of(2.0),
): Boolean {
    val error = getAngleError(target)
    return kotlin.math.abs(error.degrees) < tolerance.`in`(Units.Degrees)
}

/**
 * Extension to get the shortest angular distance to target.
 *
 * Returns a value between -180 and 180 degrees.
 *
 * @param target Target angle.
 * @return Shortest angular distance in degrees.
 */
fun Engu.shortestDistanceTo(target: Rotation2d): Double {
    val error = (target - rotation).degrees
    return when {
        error > 180 -> error - 360
        error < -180 -> error + 360
        else -> error
    }
}

/**
 * Extension to log encoder information to a string.
 *
 * Useful for debugging and diagnostics.
 *
 * @return Formatted string with encoder information.
 */
fun Engu.toDebugString(): String =
    buildString {
        append("Position: ${degrees.format(2)}° (${rotations.format(3)} rot)")
        append(" | Velocity: ${velocityDPS.format(1)}°/s")
        append(" | Offset: ${offset.degrees.format(2)}°")
    }

private fun Double.format(decimals: Int): String = "%.${decimals}f".format(this)
