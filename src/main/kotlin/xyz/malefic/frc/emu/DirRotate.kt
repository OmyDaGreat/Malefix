package xyz.malefic.frc.emu

/**
 * Enum representing the direction of rotation.
 *
 * Used by [Dir8], [DirYZ], and [DirXY] for rotation operations.
 */
enum class DirRotate {
    CLOCKWISE,
    COUNTERCLOCKWISE,
    ;

    /**
     * Returns the opposite rotation direction.
     *
     * @return The opposite [DirRotate] value.
     */
    fun opposite() = entries[(ordinal + 1) % entries.size]
}
