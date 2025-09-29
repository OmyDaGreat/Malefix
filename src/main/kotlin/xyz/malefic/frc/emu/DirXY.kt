package xyz.malefic.frc.emu

/**
 * Enum representing the four cardinal directions.
 */
enum class DirXY {
    FORWARD,
    RIGHT,
    BACKWARD,
    LEFT,
    ;

    /**
     * Function to get the opposite direction for [DirXY].
     *
     * @return The opposite direction of the current [DirXY] value.
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
     * Rotates the current direction based on the given rotation direction.
     *
     * @param dirRotate The direction to rotate ([DirRotate.CLOCKWISE] or [DirRotate.COUNTERCLOCKWISE]).
     * @return The new direction after rotation.
     */
    fun rotate(dirRotate: DirRotate) =
        entries[
            when (dirRotate) {
                DirRotate.CLOCKWISE -> (this.ordinal + 1) % entries.size
                DirRotate.COUNTERCLOCKWISE -> (this.ordinal - 1 + entries.size) % entries.size
            },
        ]
}
