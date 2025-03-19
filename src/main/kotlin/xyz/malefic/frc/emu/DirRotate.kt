package xyz.malefic.frc.emu

/**
 * Enum representing the direction of rotation.
 */
enum class DirRotate {
    CLOCKWISE,
    COUNTERCLOCKWISE,
    ;

    /**
     * Returns the opposite rotation direction.
     *
     * @return The opposite DirRotate value.
     */
    fun opposite() = entries[(ordinal + 1) % entries.size]
}
