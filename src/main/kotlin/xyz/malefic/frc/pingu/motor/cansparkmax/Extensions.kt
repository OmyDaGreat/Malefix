package xyz.malefic.frc.pingu.motor.cansparkmax

import edu.wpi.first.wpilibj.motorcontrol.PWMSparkMax
import xyz.malefic.frc.pingu.motor.Mongu

/**
 * A type alias for [Mongu] specialized with [PWMSparkMax].
 */
private typealias MonguSpark = Mongu<PWMSparkMax>

/**
 * Extension property to calculate the PWM voltage ratio for [MonguSpark].
 *
 * @return The ratio of the motor voltage to the battery voltage.
 */
@Suppress("kotlin:S6518")
val MonguSpark.pwm
    get() = motor.get()

/**
 * Extension property to retrieve the PWM channel for [MonguSpark].
 *
 * @return The channel number of the motor.
 */
val MonguSpark.channel
    get() = motor.channel
