package xyz.malefic.frc.emu

/**
 * Enum representing the four cardinal directions.
 */
enum class DirZY {
    UP,
    RIGHT,
    DOWN,
    LEFT,
    ;

    /**
     * Function to get the opposite direction for Dir4.
     *
     * @return The opposite direction of the current Dir4 value.
     */
    fun opposite() = entries[(this.ordinal + 2) % entries.size]

    /**
     * Function to convert Dir4 to Dir8.
     *
     * @return The corresponding Dir8 value for the current Dir4 value.
     */
    fun toDir8() =
        when (this) {
            UP -> Dir8.UP
            RIGHT -> Dir8.RIGHT
            DOWN -> Dir8.DOWN
            LEFT -> Dir8.LEFT
        }

    /**
     * Rotates the current direction based on the given rotation direction.
     *
     * @param dirRotate The direction to rotate (CLOCKWISE or COUNTERCLOCKWISE).
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
