package xyz.malefic.frc.emu

import xyz.malefic.frc.emu.DirRotate.CLOCKWISE
import xyz.malefic.frc.emu.DirRotate.COUNTERCLOCKWISE

/**
 * Enum representing the eight cardinal and intercardinal directions.
 */
enum class Dir8 {
    UP,
    UP_RIGHT,
    RIGHT,
    DOWN_RIGHT,
    DOWN,
    DOWN_LEFT,
    LEFT,
    UP_LEFT,
    ;

    /**
     * Function to get the opposite direction for Dir8.
     *
     * @return The opposite direction of the current Dir8 value.
     */
    fun opposite() = entries[(this.ordinal + 4) % entries.size]

    /**
     * Rotates the current direction based on the given rotation direction.
     *
     * @param dirRotate The direction to rotate (CLOCKWISE or COUNTERCLOCKWISE).
     * @return The new direction after rotation.
     */
    fun rotate(dirRotate: DirRotate) =
        entries[
            when (dirRotate) {
                CLOCKWISE -> (this.ordinal + 1) % entries.size
                COUNTERCLOCKWISE -> (this.ordinal - 1 + entries.size) % entries.size
            },
        ]

    /**
     * Converts the current Dir8 value to the corresponding Dir4 value based on the given rotation direction.
     *
     * The `toDir4` function maps each `Dir8` value to a base `Dir4` value and adjusts the mapping based on the specified rotation direction.
     *
     * ### How it works:
     * 1. **Determine the base Dir4 value**: The function maps each `Dir8` value to a base `Dir4` value.
     * 2. **Adjust based on rotation direction**: Depending on whether the rotation is clockwise or counterclockwise, the function adjusts the mapping to include the next or previous `Dir8` value.
     *
     * ### Examples:
     * - **Clockwise Rotation**:
     *   - `UP` and `UP_RIGHT` both map to `UP`.
     *   - `RIGHT` and `DOWN_RIGHT` both map to `RIGHT`.
     *   - `DOWN` and `DOWN_LEFT` both map to `DOWN`.
     *   - `LEFT` and `UP_LEFT` both map to `LEFT`.
     *
     * - **Counterclockwise Rotation**:
     *   - `UP` and `UP_LEFT` both map to `UP`.
     *   - `RIGHT` and `UP_RIGHT` both map to `RIGHT`.
     *   - `DOWN` and `DOWN_RIGHT` both map to `DOWN`.
     *   - `LEFT` and `DOWN_LEFT` both map to `LEFT`.
     *
     * @param dirRotate The direction to rotate (CLOCKWISE or COUNTERCLOCKWISE).
     * @return The corresponding Dir4 value.
     */
    fun toDir4(dirRotate: DirRotate) =
        DirZY.entries[
            when (this) {
                UP -> 0
                UP_RIGHT -> if (dirRotate == CLOCKWISE) 0 else 1
                RIGHT -> 1
                DOWN_RIGHT -> if (dirRotate == CLOCKWISE) 1 else 2
                DOWN -> 2
                DOWN_LEFT -> if (dirRotate == CLOCKWISE) 2 else 3
                LEFT -> 3
                UP_LEFT -> if (dirRotate == CLOCKWISE) 3 else 0
            },
        ]
}
