package xyz.malefic.frc.pingu.motor.talonfx

import com.ctre.phoenix6.StatusCode
import com.ctre.phoenix6.controls.ControlRequest
import com.ctre.phoenix6.hardware.TalonFX
import xyz.malefic.frc.pingu.motor.Mongu

/** Type alias for a [Mongu] motor controller specifically using [TalonFX] hardware. */
typealias MonguFX = Mongu<TalonFX>

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
