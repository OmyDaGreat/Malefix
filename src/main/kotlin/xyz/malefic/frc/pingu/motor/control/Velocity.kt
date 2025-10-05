package xyz.malefic.frc.pingu.motor.control

import xyz.malefic.frc.pingu.motor.MonguControl

/**
 * Class [Velocity] represents a type used for position control.
 */
class Velocity

/**
 * Class [MonguControlVelocity] implements [MonguControl] for the [Velocity] type.
 */
class MonguControlVelocity : MonguControl<Velocity> {
    /**
     * The velocity value to be applied.
     */
    override var value: Double = 0.0
}

/**
 * Extension property to convert a [Number] value into a [MonguControlVelocity] instance.
 *
 * This property creates a new [MonguControlVelocity] object and assigns the [Number] value
 * to its [MonguControlVelocity.value] property.
 */
@Deprecated(
    message = "Control types are now locked at Mongu initialization. Use move(Double) instead.",
    level = DeprecationLevel.WARNING,
)
val Number.velocity: MonguControlVelocity
    get() = MonguControlVelocity().apply { value = this@velocity.toDouble() }
