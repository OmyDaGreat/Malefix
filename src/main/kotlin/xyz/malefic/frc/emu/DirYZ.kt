package xyz.malefic.frc.emu

/**
 * Enum representing the four cardinal directions.
 */
enum class DirYZ {
    UP,
    RIGHT,
    DOWN,
    LEFT,
    ;

    /**
     * Function to get the opposite direction for [DirYZ].
     *
     * @return The opposite direction of the current [DirYZ] value.
     */
    fun opposite() = entries[(this.ordinal + 2) % entries.size]

    /**
     * Function to convert [DirYZ] to [Dir8].
     *
     * @return The corresponding [Dir8] value for the current [DirYZ] value.
     */
    fun toDir8() =
        when (this) {
            UP -> Dir8.UP
            RIGHT -> Dir8.RIGHT
            DOWN -> Dir8.DOWN
            LEFT -> Dir8.LEFT
        }

    /**
     * Converts the current [DirYZ] direction to its corresponding [DirXY] value.
     *
     * @return The [DirXY] value that matches the current [DirYZ] direction.
     */
    fun toDirXY() =
        when (this) {
            UP -> DirXY.FORWARD
            RIGHT -> DirXY.RIGHT
            DOWN -> DirXY.BACKWARD
            LEFT -> DirXY.LEFT
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
