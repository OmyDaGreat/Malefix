package xyz.malefic.frc.emu

/**
 * Enum representing the four cardinal directions in the YZ plane.
 *
 * Provides conversion and rotation utilities to [Dir8], [DirXY], and [DirRotate].
 */
enum class DirYZ {
    UP,
    RIGHT,
    DOWN,
    LEFT,
    ;

    /**
     * Returns the opposite direction of the current [DirYZ] value.
     *
     * @return The opposite [DirYZ] direction.
     */
    fun opposite() = entries[(this.ordinal + 2) % entries.size]

    /**
     * Converts this [DirYZ] to the corresponding [Dir8] direction.
     *
     * @return The corresponding [Dir8] value.
     */
    fun toDir8() =
        when (this) {
            UP -> Dir8.UP
            RIGHT -> Dir8.RIGHT
            DOWN -> Dir8.DOWN
            LEFT -> Dir8.LEFT
        }

    /**
     * Converts this [DirYZ] direction to its corresponding [DirXY] value.
     *
     * @return The [DirXY] value that matches this [DirYZ] direction.
     */
    fun toDirXY() =
        when (this) {
            UP -> DirXY.FORWARD
            RIGHT -> DirXY.RIGHT
            DOWN -> DirXY.BACKWARD
            LEFT -> DirXY.LEFT
        }

    /**
     * Rotates the current direction based on the given [DirRotate] direction.
     *
     * @param dirRotate The direction to rotate ([DirRotate.CLOCKWISE] or [DirRotate.COUNTERCLOCKWISE]).
     * @return The new [DirYZ] direction after rotation.
     */
    fun rotate(dirRotate: DirRotate) =
        entries[
            when (dirRotate) {
                DirRotate.CLOCKWISE -> (this.ordinal + 1) % entries.size
                DirRotate.COUNTERCLOCKWISE -> (this.ordinal - 1 + entries.size) % entries.size
            },
        ]
}
