package xyz.malefic.frc.emu

import xyz.malefic.frc.emu.DirRotate.CLOCKWISE
import xyz.malefic.frc.emu.DirRotate.COUNTERCLOCKWISE
import kotlin.test.Test
import kotlin.test.assertEquals

class DirectionTests {
    @Test
    fun testDir4Opposite() {
        assertEquals(Dir4.DOWN, Dir4.UP.opposite())
        assertEquals(Dir4.LEFT, Dir4.RIGHT.opposite())
        assertEquals(Dir4.UP, Dir4.DOWN.opposite())
        assertEquals(Dir4.RIGHT, Dir4.LEFT.opposite())
    }

    @Test
    fun testDir4Rotate() {
        assertEquals(Dir4.RIGHT, Dir4.UP.rotate(CLOCKWISE))
        assertEquals(Dir4.LEFT, Dir4.UP.rotate(COUNTERCLOCKWISE))
    }

    @Test
    fun testDir4ToDir8() {
        assertEquals(Dir8.UP, Dir4.UP.toDir8())
        assertEquals(Dir8.RIGHT, Dir4.RIGHT.toDir8())
        assertEquals(Dir8.DOWN, Dir4.DOWN.toDir8())
        assertEquals(Dir8.LEFT, Dir4.LEFT.toDir8())
    }

    @Test
    fun testDir8Opposite() {
        assertEquals(Dir8.DOWN, Dir8.UP.opposite())
        assertEquals(Dir8.UP_LEFT, Dir8.DOWN_RIGHT.opposite())
    }

    @Test
    fun testDir8Rotate() {
        assertEquals(Dir8.UP_RIGHT, Dir8.UP.rotate(CLOCKWISE))
        assertEquals(Dir8.UP_LEFT, Dir8.UP.rotate(COUNTERCLOCKWISE))
    }

    @Test
    fun testDir8ToDir4() {
        assertEquals(Dir4.UP, Dir8.UP.toDir4(CLOCKWISE))
        assertEquals(Dir4.RIGHT, Dir8.DOWN_RIGHT.toDir4(CLOCKWISE))
        assertEquals(Dir4.DOWN, Dir8.DOWN.toDir4(CLOCKWISE))
        assertEquals(Dir4.LEFT, Dir8.UP_LEFT.toDir4(CLOCKWISE))
    }

    @Test
    fun testDirRotateOpposite() {
        assertEquals(COUNTERCLOCKWISE, CLOCKWISE.opposite())
        assertEquals(CLOCKWISE, COUNTERCLOCKWISE.opposite())
    }
}
