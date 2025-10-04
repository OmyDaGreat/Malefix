package xyz.malefic.frc.pingu.motor.talonfx

import com.ctre.phoenix6.StatusCode
import com.ctre.phoenix6.controls.ControlRequest
import com.ctre.phoenix6.hardware.TalonFX
import xyz.malefic.frc.pingu.motor.Mongu

/** Type alias for a [Mongu] motor controller specifically using [TalonFX] hardware. */
private typealias MonguFX = Mongu<TalonFX>

/**
 * Sets the [ControlRequest] for the underlying [TalonFX] motor.
 *
 * @receiver The [MonguFX] instance representing the motor controller.
 * @param control The [ControlRequest] to apply to the motor.
 * @return The [StatusCode] indicating the result of the operation.
 */
fun MonguFX.setControl(control: ControlRequest): StatusCode = motor.setControl(control)

/**
 * Retrieves the current position of the [TalonFX] motor.
 *
 * @receiver The [MonguFX] instance representing the motor controller.
 * @return The current position of the motor as a [Double].
 */
val MonguFX.position
    get() = motor.position.valueAsDouble

/**
 * Retrieves the current velocity of the [TalonFX] motor.
 *
 * @receiver The [MonguFX] instance representing the motor controller.
 * @return The current velocity of the motor as a [Double].
 */
val MonguFX.velocity
    get() = motor.velocity.valueAsDouble

/**
 * Retrieves the current acceleration of the [TalonFX] motor.
 *
 * @receiver The [MonguFX] instance representing the motor controller.
 * @return The current acceleration of the motor as a [Double].
 */
val MonguFX.acceleration
    get() = motor.acceleration.valueAsDouble

/**
 * Retrieves the custom `pingu` property from the [TalonFXConfig] configuration.
 *
 * @receiver The [MonguFX] instance representing the motor controller.
 * @return The `pingu` value from the [TalonFXConfig].
 */
val MonguFX.pingu
    get() = (configuration as TalonFXConfig).pingu

/**
 * Retrieves the device ID of the underlying [TalonFX] motor.
 *
 * @receiver The [MonguFX] instance representing the motor controller.
 * @return The device ID as an [Int].
 */
val MonguFX.deviceID
    get() = motor.deviceID

/**
 * Retrieves the stator current of the [TalonFX] motor.
 *
 * @receiver The [MonguFX] instance representing the motor controller.
 * @return The stator current as a [Double].
 */
val MonguFX.statorCurrent
    get() = motor.statorCurrent.valueAsDouble

/**
 * Retrieves the supply current of the [TalonFX] motor.
 *
 * @receiver The [MonguFX] instance representing the motor controller.
 * @return The supply current as a [Double].
 */
val MonguFX.supplyCurrent
    get() = motor.supplyCurrent.valueAsDouble

/**
 * Retrieves the stall current of the [TalonFX] motor.
 *
 * @receiver The [MonguFX] instance representing the motor controller.
 * @return The stall current as a [Double].
 */
val MonguFX.motorStallCurrent
    get() = motor.motorStallCurrent.valueAsDouble

/**
 * Indicates whether the [TalonFX] motor is currently connected.
 *
 * @receiver The [MonguFX] instance representing the motor controller.
 * @return `true` if the motor is connected, `false` otherwise.
 */
val MonguFX.isConnected
    get() = motor.isConnected

/**
 * Retrieves the supply voltage of the [TalonFX] motor.
 *
 * @receiver The [MonguFX] instance representing the motor controller.
 * @return The supply voltage as a [Double].
 */
val MonguFX.supplyVoltage
    get() = motor.supplyVoltage.valueAsDouble

/**
 * Retrieves the motor voltage of the [TalonFX] motor.
 *
 * @receiver The [MonguFX] instance representing the motor controller.
 * @return The motor voltage as a [Double].
 */
val MonguFX.motorVoltage
    get() = motor.motorVoltage.valueAsDouble
