package xyz.malefic.frc.emu

import xyz.malefic.frc.emu.DirRotate.CLOCKWISE
import xyz.malefic.frc.emu.DirRotate.COUNTERCLOCKWISE
import kotlin.test.Test
import kotlin.test.assertEquals

class DirectionTests {
    @Test
    fun testDir4Opposite() {
        assertEquals(DirYZ.DOWN, DirYZ.UP.opposite())
        assertEquals(DirYZ.LEFT, DirYZ.RIGHT.opposite())
        assertEquals(DirYZ.UP, DirYZ.DOWN.opposite())
        assertEquals(DirYZ.RIGHT, DirYZ.LEFT.opposite())
    }

    @Test
    fun testDir4Rotate() {
        assertEquals(DirYZ.RIGHT, DirYZ.UP.rotate(CLOCKWISE))
        assertEquals(DirYZ.LEFT, DirYZ.UP.rotate(COUNTERCLOCKWISE))
    }

    @Test
    fun testDir4ToDir8() {
        assertEquals(Dir8.UP, DirYZ.UP.toDir8())
        assertEquals(Dir8.RIGHT, DirYZ.RIGHT.toDir8())
        assertEquals(Dir8.DOWN, DirYZ.DOWN.toDir8())
        assertEquals(Dir8.LEFT, DirYZ.LEFT.toDir8())
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
        assertEquals(DirYZ.UP, Dir8.UP.toDir4(CLOCKWISE))
        assertEquals(DirYZ.RIGHT, Dir8.DOWN_RIGHT.toDir4(CLOCKWISE))
        assertEquals(DirYZ.DOWN, Dir8.DOWN.toDir4(CLOCKWISE))
        assertEquals(DirYZ.LEFT, Dir8.UP_LEFT.toDir4(CLOCKWISE))
    }

    @Test
    fun testDirRotateOpposite() {
        assertEquals(COUNTERCLOCKWISE, CLOCKWISE.opposite())
        assertEquals(CLOCKWISE, COUNTERCLOCKWISE.opposite())
    }
}
