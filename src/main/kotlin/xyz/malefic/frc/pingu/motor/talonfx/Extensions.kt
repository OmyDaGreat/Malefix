package xyz.malefic.frc.pingu.motor.talonfx

import com.ctre.phoenix6.StatusCode

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
 * @receiver The [TonguFX] instance representing the motor controller.
 * @param position The position to set the motor to. Defaults to 0.0.
 * @return The [StatusCode] indicating the result of the operation.
 */
fun TonguFX.resetPosition(position: Double = 0.0): StatusCode = setPosition(position)
