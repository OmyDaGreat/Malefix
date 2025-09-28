package xyz.malefic.frc.extension

import com.ctre.phoenix6.configs.CANcoderConfiguration
import com.ctre.phoenix6.hardware.CANcoder
import com.ctre.phoenix6.signals.SensorDirectionValue

/**
 * Extension function to configure a CANcoderConfiguration sensor with nearly all relevant options.
 *
 * @param sensorDirection Direction the sensor should consider positive
 * @param magnetOffset Offset to apply to the magnet sensor reading
 * @param discontinuityPoint Point at which the sensor reading resets to low point
 * @param extraConfig Optional lambda for further CANcoderConfiguration customization.
 */
fun CANcoder.configureWithDefaults(
    sensorDirection: SensorDirectionValue = SensorDirectionValue.CounterClockwise_Positive,
    magnetOffset: Double = 0.0,
    discontinuityPoint: Double = 1.0,
    extraConfig: (CANcoderConfiguration.() -> Unit)? = null,
) {
    val config = CANcoderConfiguration()

    // Magnet Sensor
    config.MagnetSensor.SensorDirection = sensorDirection
    config.MagnetSensor.MagnetOffset = magnetOffset
    config.MagnetSensor.AbsoluteSensorDiscontinuityPoint = discontinuityPoint

    // Extra customization if needed
    extraConfig?.let { config.it() }

    // Apply configuration
    this.configurator.apply(config)
}
