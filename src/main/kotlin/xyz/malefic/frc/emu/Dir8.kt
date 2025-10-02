package xyz.malefic.frc.emu

import xyz.malefic.frc.emu.DirRotate.CLOCKWISE
import xyz.malefic.frc.emu.DirRotate.COUNTERCLOCKWISE

/**
 * Enum representing the eight cardinal and intercardinal directions.
 *
 * Provides conversion and rotation utilities to [DirYZ], [DirRotate], and related types.
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
     * Returns the opposite direction of the current [Dir8] value.
     *
     * @return The opposite [Dir8] direction.
     */
    fun opposite() = entries[(this.ordinal + 4) % entries.size]

    /**
     * Rotates the current direction based on the given [DirRotate] direction.
     *
     * @param dirRotate The direction to rotate ([DirRotate.CLOCKWISE] or [DirRotate.COUNTERCLOCKWISE]).
     * @return The new [Dir8] direction after rotation.
     */
    fun rotate(dirRotate: DirRotate) =
        entries[
            when (dirRotate) {
                CLOCKWISE -> (this.ordinal + 1) % entries.size
                COUNTERCLOCKWISE -> (this.ordinal - 1 + entries.size) % entries.size
            },
        ]

    /**
     * Converts the current [Dir8] value to the corresponding [DirYZ] value based on the given [DirRotate] direction.
     *
     * The [toDirYZ] function maps each [Dir8] value to a base [DirYZ] value and adjusts the mapping based on the specified rotation direction.
     *
     * @return The corresponding [DirYZ] value.
     */
    fun toDirYZ(dirRotate: DirRotate) =
        DirYZ.entries[
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
