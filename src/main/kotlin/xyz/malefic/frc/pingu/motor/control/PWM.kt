package xyz.malefic.frc.pingu.motor.control

import xyz.malefic.frc.pingu.motor.MonguControl

/**
 * Class [PWM] represents a type used for PWM control.
 */
class PWM

/**
 * Class [MonguControlPWM] implements [MonguControl] for the [PWM] type.
 */
class MonguControlPWM : MonguControl<PWM> {
    /**
     * The PWM value to be applied.
     */
    override var value: Double = 0.0
}

/**
 * Extension property to convert a [Number] value into a [MonguControlPWM] instance.
 *
 * This property creates a new [MonguControlPWM] object and assigns the [Number] value
 * to its [MonguControlPWM.value] property.
 */
val Number.pwm: MonguControlPWM
    get() = MonguControlPWM().apply { value = this@pwm.toDouble() }
