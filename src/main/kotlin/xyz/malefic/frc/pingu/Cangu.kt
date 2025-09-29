package xyz.malefic.frc.pingu

import com.ctre.phoenix6.configs.CANcoderConfiguration
import com.ctre.phoenix6.hardware.CANcoder
import com.ctre.phoenix6.signals.SensorDirectionValue

/**
 * A generic [CANcoder] wrapper class that allows configuration of [CANcoder] sensors.
 *
 * Make sure to configure the sensor after instantiation to apply desired settings.
 *
 * @property cancoder The [CANcoder] instance being wrapped.
 */
class Cangu(
    val cancoder: CANcoder,
) {
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
     * Configures the [CANcoder] using explicit parameters.
     *
     * @param sensorDirection Direction the sensor should consider positive
     * @param magnetOffset Offset to apply to the magnet sensor reading
     * @param discontinuityPoint Point at which the sensor reading resets to low point
     * @param extraConfig Optional lambda for further [CANcoderConfiguration] customization.
     */
    fun configure(
        sensorDirection: SensorDirectionValue = SensorDirectionValue.CounterClockwise_Positive,
        magnetOffset: Double = 0.0,
        discontinuityPoint: Double = 1.0,
        extraConfig: (CANcoderConfiguration.() -> Unit)? = null,
    ) {
        val config = CANcoderConfiguration()
        config.MagnetSensor.SensorDirection = sensorDirection
        config.MagnetSensor.MagnetOffset = magnetOffset
        config.MagnetSensor.AbsoluteSensorDiscontinuityPoint = discontinuityPoint
        extraConfig?.invoke(config)
        cancoder.configurator.apply(config)
        configuration = config
    }
}
