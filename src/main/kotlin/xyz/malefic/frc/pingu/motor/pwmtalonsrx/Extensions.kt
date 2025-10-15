package xyz.malefic.frc.pingu.motor.pwmtalonsrx

/**
 * Extension property to calculate the PWM voltage ratio for [TalonuSRX].
 *
 * @return The ratio of the motor voltage to the battery voltage.
 */
@Suppress("kotlin:S6518")
val TalonuSRX.pwm
    get() = get()
