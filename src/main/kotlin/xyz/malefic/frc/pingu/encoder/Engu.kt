package xyz.malefic.frc.pingu.encoder

import com.ctre.phoenix6.CANBus
import com.ctre.phoenix6.configs.CANcoderConfiguration
import com.ctre.phoenix6.hardware.CANcoder

/**
 * A generic [CANcoder] wrapper class that allows configuration of [CANcoder] sensors.
 *
 * Make sure to configure the sensor after instantiation to apply desired settings.
 */
class Engu(
    id: Int,
    device: String = "",
) : CANcoder(id, device) {
    constructor(id: Int, canBus: CANBus) : this(id, canBus.name)

    /**
     * Holds the last configuration applied to this [CANcoder].
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
     * Example:
     * ```
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
        val config = CANcoderConfiguration().apply(block)
        configurator.apply(config)
        configuration = config
    }
}
