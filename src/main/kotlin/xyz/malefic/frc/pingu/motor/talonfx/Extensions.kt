@file:Suppress("unused")

package xyz.malefic.frc.pingu.motor.talonfx

import com.ctre.phoenix6.StatusCode
import com.ctre.phoenix6.controls.ControlRequest

/**
 * Retrieves the custom `pingu` property from the [TalonFXConfig] configuration.
 *
 * @receiver The [TonguFX] instance representing the motor controller.
 * @return The `pingu` value from the [TalonFXConfig].
 */
val TonguFX.pingu
    get() = configuration.pingu

/**
 * Resets the position of the [TonguFX] motor to the specified value.
 *
 * This extension function calls [TonguFX.setPosition] with the provided position,
 *
 * @receiver The [TonguFX] instance representing the motor controller.
 * @param position The position to set the motor to. Defaults to 0.0.
 * @return The [StatusCode] indicating the result of the operation.
 */
fun TonguFX.resetPosition(position: Double = 0.0): StatusCode = setPosition(position)

/**
 * Invokes a control request on the [TonguFX] motor controller using operator call syntax.
 *
 * This extension operator forwards the provided [ControlRequest] to [TonguFX.setControl].
 *
 * @receiver The [TonguFX] instance representing the motor controller.
 * @param control The control request to send.
 * @return The [StatusCode] result from [TonguFX.setControl].
 */
operator fun TonguFX.invoke(control: ControlRequest): StatusCode = setControl(control)
