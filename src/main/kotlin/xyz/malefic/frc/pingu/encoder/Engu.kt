package xyz.malefic.frc.pingu.encoder

import com.ctre.phoenix6.CANBus
import com.ctre.phoenix6.configs.CANcoderConfiguration
import com.ctre.phoenix6.hardware.CANcoder

/**
 * A generic [CANcoder] wrapper class that allows configuration of such sensors.
 *
 * Make sure to configure the sensor after instantiation to apply desired settings.
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
     * If no prior configuration block was provided, it applies on top of the default configuration. Otherwise, it applies the custom settings defined in the block on top of the previous configuration.
     *
     * Example:
     * ```kotlin
     * engu.configure {
     *     MagnetSensor.SensorDirection = SensorDirectionValue.Clockwise_Positive
     *     MagnetSensor.MagnetOffset = 0.5
     *     // ...other configuration...
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
}
