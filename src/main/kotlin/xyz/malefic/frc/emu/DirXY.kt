package xyz.malefic.frc.emu

/**
 * Enum representing the four cardinal directions in the XY plane.
 *
 * Provides conversion and rotation utilities to [DirYZ] and [DirRotate].
 */
enum class DirXY {
    FORWARD,
    RIGHT,
    BACKWARD,
    LEFT,
    ;

    /**
     * Returns the opposite direction of the current [DirXY] value.
     *
     * @return The opposite [DirXY] direction.
     */
    fun opposite() = entries[(this.ordinal + 2) % entries.size]

    /**
     * Maps the current [DirXY] direction to its corresponding [DirYZ] direction.
     *
     * @return The corresponding [DirYZ] value for this [DirXY].
     */
    fun toDirYZ() =
        when (this) {
            FORWARD -> DirYZ.UP
            BACKWARD -> DirYZ.DOWN
            RIGHT -> DirYZ.RIGHT
            LEFT -> DirYZ.LEFT
        }

    /**
     * Rotates the current direction based on the given [DirRotate] direction.
     *
     * @param dirRotate The direction to rotate ([DirRotate.CLOCKWISE] or [DirRotate.COUNTERCLOCKWISE]).
     * @return The new [DirXY] direction after rotation.
     */
    fun rotate(dirRotate: DirRotate) =
        entries[
            when (dirRotate) {
                DirRotate.CLOCKWISE -> (this.ordinal + 1) % entries.size
                DirRotate.COUNTERCLOCKWISE -> (this.ordinal - 1 + entries.size) % entries.size
            },
        ]
}
