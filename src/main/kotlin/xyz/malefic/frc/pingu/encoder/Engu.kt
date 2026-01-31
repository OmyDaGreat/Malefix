package xyz.malefic.frc.pingu.encoder

import com.ctre.phoenix6.CANBus
import com.ctre.phoenix6.configs.CANcoderConfiguration
import com.ctre.phoenix6.hardware.CANcoder
import com.ctre.phoenix6.signals.SensorDirectionValue
import edu.wpi.first.math.geometry.Rotation2d
import edu.wpi.first.units.Measure
import edu.wpi.first.units.Units
import edu.wpi.first.units.measure.Angle
import edu.wpi.first.units.measure.AngularVelocity
import kotlin.math.PI

/**
 * Enhanced [CANcoder] wrapper class with simplified configuration and utility methods.
 *
 * Provides convenient methods for common encoder operations like angle reading,
 * offset calibration, and unit conversions specifically useful for swerve drive modules.
 *
 * ## Usage Examples:
 * ```kotlin
 * // Basic setup
 * val encoder = Engu(1)
 *
 * // Configure with offset and direction
 * encoder.configure {
 *     MagnetSensor.apply {
 *         SensorDirection = SensorDirectionValue.CounterClockwise_Positive
 *         MagnetOffset = 0.25
 *     }
 * }
 *
 * // Get angle as Rotation2d
 * val angle = encoder.rotation
 *
 * // Get position in rotations
 * val rotations = encoder.rotations
 *
 * // Get position in degrees
 * val degrees = encoder.degrees
 * ```
 *
 * @property id The CAN device ID of the CANcoder.
 * @property canbus The CAN bus to use (default is `roboRIO`).
 */
class Engu(
    id: Int,
    canbus: CANBus = CANBus.roboRIO(),
) : CANcoder(id, canbus) {
    /**
     * Holds the last [CANcoderConfiguration] applied to this [CANcoder].
     * This property is updated whenever the [configure] method is called.
     * It allows retrieval of the configuration settings for inspection or reuse.
     */
    lateinit var configuration: CANcoderConfiguration

    init {
        configure()
    }

    /**
     * Configures the [CANcoder] using a DSL-style configuration block.
     *
     * If no prior configuration block was provided, it applies on top of the default configuration.
     * Otherwise, it applies the custom settings defined in the block on top of the previous configuration.
     *
     * Example:
     * ```kotlin
     * engu.configure {
     *     MagnetSensor.SensorDirection = SensorDirectionValue.Clockwise_Positive
     *     MagnetSensor.MagnetOffset = 0.5
     * }
     * ```
     *
     * @param block Lambda with receiver for [CANcoderConfiguration] to customize settings.
     */
    fun configure(block: CANcoderConfiguration.() -> Unit = {}) {
        val config = (if (::configuration.isInitialized) configuration else CANcoderConfiguration()).apply(block)
        configurator.apply(config)
        configuration = config
    }

    /**
     * Gets the absolute position of the encoder as a [Rotation2d].
     *
     * This is particularly useful for swerve modules where angles are commonly
     * represented as [Rotation2d] objects.
     *
     * @return Current absolute position as [Rotation2d].
     */
    val rotation: Rotation2d
        get() = Rotation2d.fromRotations(absolutePosition.valueAsDouble)

    /**
     * Gets the absolute position in rotations (0.0 to 1.0).
     *
     * @return Current absolute position in rotations.
     */
    val rotations: Double
        get() = absolutePosition.valueAsDouble

    /**
     * Gets the absolute position in degrees (0.0 to 360.0).
     *
     * @return Current absolute position in degrees.
     */
    val degrees: Double
        get() = absolutePosition.valueAsDouble * 360.0

    /**
     * Gets the absolute position in radians (0.0 to 2π).
     *
     * @return Current absolute position in radians.
     */
    val radians: Double
        get() = absolutePosition.valueAsDouble * 2.0 * PI

    /**
     * Gets the absolute position as a measured angle.
     *
     * @return Current absolute position as [Measure]<[Angle]>.
     */
    val angle: Angle
        get() = Units.Rotations.of(absolutePosition.valueAsDouble)

    /**
     * Gets the velocity in rotations per second.
     *
     * @return Current velocity in rotations per second.
     */
    val velocityRPS: Double
        get() = velocity.valueAsDouble

    /**
     * Gets the velocity in degrees per second.
     *
     * @return Current velocity in degrees per second.
     */
    val velocityDPS: Double
        get() = velocity.valueAsDouble * 360.0

    /**
     * Gets the velocity in radians per second.
     *
     * @return Current velocity in radians per second.
     */
    val velocityRadPS: Double
        get() = velocity.valueAsDouble * 2.0 * PI

    /**
     * Gets the angular velocity as a measured value.
     *
     * @return Current angular velocity as [Measure]<[AngularVelocity]>.
     */
    val angularVelocity: AngularVelocity
        get() = Units.RotationsPerSecond.of(velocity.valueAsDouble)

    /**
     * Configures the encoder for swerve module use with common settings.
     *
     * Applies standard configuration for swerve modules including:
     * - Counter-clockwise positive direction
     * - Provided magnet offset
     *
     * Example:
     * ```kotlin
     * val encoder = Engu(1)
     * encoder.configureSwerve(Rotation2d.fromDegrees(45.0))
     * ```
     *
     * @param offset The magnet offset as a [Rotation2d].
     * @param clockwisePositive Whether clockwise should be positive (default: false).
     */
    fun configureSwerve(
        offset: Rotation2d = Rotation2d(),
        clockwisePositive: Boolean = false,
    ) {
        configure {
            MagnetSensor.apply {
                SensorDirection =
                    if (clockwisePositive) {
                        SensorDirectionValue.Clockwise_Positive
                    } else {
                        SensorDirectionValue.CounterClockwise_Positive
                    }
                MagnetOffset = offset.rotations
            }
        }
    }

    /**
     * Sets the magnet offset to the current position.
     *
     * This is useful for calibrating swerve modules - align the module to the desired
     * zero position and call this method to set that as the offset.
     *
     * Example:
     * ```kotlin
     * // Manually align module to forward position
     * encoder.calibrateToCurrentPosition()
     * ```
     */
    fun calibrateToCurrentPosition() {
        val currentPosition = absolutePosition.valueAsDouble
        configure {
            MagnetSensor.MagnetOffset = currentPosition
        }
    }

    /**
     * Sets a specific offset in various units.
     *
     * @param offset The offset to set as a [Rotation2d].
     */
    fun setOffset(offset: Rotation2d) {
        configure {
            MagnetSensor.MagnetOffset = offset.rotations
        }
    }

    /**
     * Sets a specific offset in rotations.
     *
     * @param rotations The offset in rotations (0.0 to 1.0).
     */
    fun setOffsetRotations(rotations: Double) {
        configure {
            MagnetSensor.MagnetOffset = rotations
        }
    }

    /**
     * Sets a specific offset in degrees.
     *
     * @param degrees The offset in degrees (0.0 to 360.0).
     */
    fun setOffsetDegrees(degrees: Double) {
        configure {
            MagnetSensor.MagnetOffset = degrees / 360.0
        }
    }

    /**
     * Gets the current magnet offset.
     *
     * @return Current magnet offset as [Rotation2d].
     */
    val offset: Rotation2d
        get() = Rotation2d.fromRotations(configuration.MagnetSensor.MagnetOffset)
}
