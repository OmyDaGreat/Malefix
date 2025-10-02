package xyz.malefic.frc.pingu.motor.control

import xyz.malefic.frc.pingu.motor.MonguControl

/**
 * Class [Voltage] represents a type used for voltage control.
 */
class Voltage

/**
 * Class [MonguControlVoltage] implements [MonguControl] for the [Voltage] type.
 */
class MonguControlVoltage : MonguControl<Voltage> {
    /**
     * The voltage value to be applied.
     */
    override var value: Double = 0.0
}

/**
 * Extension property to convert a [Number] value into a [MonguControlVoltage] instance.
 *
 * This property creates a new [MonguControlVoltage] object and assigns the [Number] value
 * to its [MonguControlVoltage.value] property.
 */
val Number.voltage: MonguControlVoltage
    get() = MonguControlVoltage().apply { value = this@voltage.toDouble() }
