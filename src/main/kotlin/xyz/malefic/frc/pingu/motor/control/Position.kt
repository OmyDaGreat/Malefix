package xyz.malefic.frc.pingu.motor.control

import xyz.malefic.frc.pingu.motor.MonguControl

/**
 * Class [Position] represents a type used for position control.
 */
class Position

/**
 * Class [MonguControlPosition] implements [MonguControl] for the [Position] type.
 *
 * It contains a [value] property to store the position value.
 */
class MonguControlPosition : MonguControl<Position> {
    /**
     * The position value to be applied.
     */
    override var value: Double = 0.0
}

/**
 * Extension property to convert a [Number] value into a [MonguControlPosition] instance.
 *
 * This property creates a new [MonguControlPosition] object and assigns the [Number] value
 * to its [MonguControlPosition.value] property.
 */
val Number.position: MonguControlPosition
    get() = MonguControlPosition().apply { value = this@position.toDouble() }
