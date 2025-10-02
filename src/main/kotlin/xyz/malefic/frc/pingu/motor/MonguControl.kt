package xyz.malefic.frc.pingu.motor

/**
 * Interface `MonguControl` represents a generic control mechanism for a specific type `T`.
 *
 * @param T The type of the control mechanism.
 */
interface MonguControl<out T>

/**
 * Extension property to convert a `Double` value into a `MonguControlPWM` instance.
 *
 * This property creates a new `MonguControlPWM` object and assigns the `Double` value
 * to its `value` property.
 */
val Double.pwm: MonguControlPWM
    get() = MonguControlPWM().apply { value = this@pwm }

/**
 * Class `PWM` represents a type used for PWM control.
 */
class PWM

/**
 * Class `MonguControlPWM` implements `MonguControl` for the `PWM` type.
 *
 * It contains a `value` property to store the PWM value.
 */
class MonguControlPWM : MonguControl<PWM> {
    /**
     * The PWM value to be applied.
     */
    var value: Double = 0.0
}

/**
 * Class `Voltage` represents a type used for voltage control.
 */
class Voltage

/**
 * Class `MonguControlVoltage` implements `MonguControl` for the `Voltage` type.
 *
 * It contains a `value` property to store the voltage value.
 */
class MonguControlVoltage : MonguControl<Voltage> {
    /**
     * The voltage value to be applied.
     */
    var value: Double = 0.0
}

/**
 * Extension property to convert a `Double` value into a `MonguControlVoltage` instance.
 *
 * This property creates a new `MonguControlVoltage` object and assigns the `Double` value
 * to its `value` property.
 */
val Double.voltage: MonguControlVoltage
    get() = MonguControlVoltage().apply { value = this@voltage }

/**
 * Class `Position` represents a type used for position control.
 */
class Position

/**
 * Class `MonguControlPosition` implements `MonguControl` for the `Position` type.
 *
 * It contains a `value` property to store the position value.
 */
class MonguControlPosition : MonguControl<Position> {
    /**
     * The position value to be applied.
     */
    var value: Double = 0.0
}

/**
 * Extension property to convert a `Double` value into a `MonguControlPosition` instance.
 *
 * This property creates a new `MonguControlPosition` object and assigns the `Double` value
 * to its `value` property.
 */
val Double.position: MonguControlPosition
    get() = MonguControlPosition().apply { value = this@position }
