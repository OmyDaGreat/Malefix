package xyz.malefic.frc.pingu.motor.pwmtalonsrx

import edu.wpi.first.wpilibj.motorcontrol.PWMTalonSRX
import xyz.malefic.frc.pingu.motor.Mongu

/**
 * A type alias for [Mongu] specialized with [PWMTalonSRX].
 */
private typealias MonguSRX = Mongu<PWMTalonSRX>

/**
 * Extension property to calculate the PWM voltage ratio for [MonguSRX].
 *
 * @return The ratio of the motor voltage to the battery voltage.
 */
@Suppress("kotlin:S6518")
val MonguSRX.pwm
    get() = motor.get()

/**
 * Extension property to retrieve the PWM channel for [MonguSRX].
 *
 * @return The channel number of the motor.
 */
val MonguSRX.channel
    get() = motor.channel
