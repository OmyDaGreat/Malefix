package xyz.malefic.frc.pingu.motor

/**
 * Represents a generic control mechanism for a specific type `T`.
 *
 * @param T The type of the control mechanism.
 */
interface MonguControl<out T> {
    var value: Double
}
