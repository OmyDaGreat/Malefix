package xyz.malefic.frc.pingu

import com.ctre.phoenix6.hardware.CANcoder
import com.ctre.phoenix6.hardware.TalonFX
import edu.wpi.first.wpilibj.Alert
import edu.wpi.first.wpilibj.Alert.AlertType.kError
import edu.wpi.first.wpilibj2.command.SubsystemBase

/**
 * Type alias for a pair consisting of a device and its corresponding alert.
 * The device can be any type, and the alert is an instance of WPILib's Alert class.
 */
typealias DeviceAlert = Pair<Any, Alert>

/**
 * FRC subsystem that monitors CTRE Phoenix 6 devices and raises alerts for disconnections.
 *
 * This singleton object tracks the connection status of TalonFX motors and CANcoders,
 * providing real-time monitoring through WPILib's alert system. It's designed for early
 * detection of CAN bus or device issues during matches and testing.
 *
 * @property devicePairs Pairs of monitored devices and their corresponding alerts
 */
object AlertPingu : SubsystemBase() {
    private val devicePairs = mutableListOf<DeviceAlert>()

    /**
     * Updates connection status for all monitored devices.
     * Called periodically by the CommandScheduler during robot operation.
     * Uses parallel processing for efficient handling of multiple devices.
     */
    override fun periodic() {
        devicePairs.stream().forEach { (device, alert) ->
            when (device) {
                is TalonFX -> alert.set(!device.isConnected)
                is CANcoder -> alert.set(!device.isConnected)
            }
        }
    }

    /**
     * Registers a TalonFX motor for connection monitoring.
     *
     * @param motor TalonFX motor instance to monitor
     * @param motorName Descriptive name for identifying the motor in Driver Station alerts
     */
    @JvmStatic
    fun add(
        motor: TalonFX,
        motorName: String,
    ) {
        val alert = Alert("Disconnected $motorName motor ${motor.deviceID}", kError)
        devicePairs.add(Pair(motor, alert))
    }

    /**
     * Registers a CANcoder for connection monitoring.
     *
     * @param canCoder CANcoder instance to monitor
     */
    @JvmStatic
    fun add(canCoder: CANcoder) {
        val alert = Alert("Disconnected CANcoder ${canCoder.deviceID}", kError)
        devicePairs.add(Pair(canCoder, alert))
    }
}
