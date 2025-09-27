package xyz.malefic.frc.emu

import xyz.malefic.frc.emu.DirRotate.CLOCKWISE
import xyz.malefic.frc.emu.DirRotate.COUNTERCLOCKWISE
import kotlin.test.Test
import kotlin.test.assertEquals

class DirectionTests {
    @Test
    fun testDir4Opposite() {
        assertEquals(DirZY.DOWN, DirZY.UP.opposite())
        assertEquals(DirZY.LEFT, DirZY.RIGHT.opposite())
        assertEquals(DirZY.UP, DirZY.DOWN.opposite())
        assertEquals(DirZY.RIGHT, DirZY.LEFT.opposite())
    }

    @Test
    fun testDir4Rotate() {
        assertEquals(DirZY.RIGHT, DirZY.UP.rotate(CLOCKWISE))
        assertEquals(DirZY.LEFT, DirZY.UP.rotate(COUNTERCLOCKWISE))
    }

    @Test
    fun testDir4ToDir8() {
        assertEquals(Dir8.UP, DirZY.UP.toDir8())
        assertEquals(Dir8.RIGHT, DirZY.RIGHT.toDir8())
        assertEquals(Dir8.DOWN, DirZY.DOWN.toDir8())
        assertEquals(Dir8.LEFT, DirZY.LEFT.toDir8())
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
        assertEquals(DirZY.UP, Dir8.UP.toDir4(CLOCKWISE))
        assertEquals(DirZY.RIGHT, Dir8.DOWN_RIGHT.toDir4(CLOCKWISE))
        assertEquals(DirZY.DOWN, Dir8.DOWN.toDir4(CLOCKWISE))
        assertEquals(DirZY.LEFT, Dir8.UP_LEFT.toDir4(CLOCKWISE))
    }

    @Test
    fun testDirRotateOpposite() {
        assertEquals(COUNTERCLOCKWISE, CLOCKWISE.opposite())
        assertEquals(CLOCKWISE, COUNTERCLOCKWISE.opposite())
    }
}
