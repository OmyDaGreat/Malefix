package xyz.malefic.frc.pingu.motor

import xyz.malefic.frc.pingu.motor.control.ControlType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MonguControlTypeTests {
    @Test
    fun testControlTypeEnumValues() {
        val types = ControlType.entries
        assertEquals(4, types.size)
        assertTrue(types.contains(ControlType.PWM))
        assertTrue(types.contains(ControlType.VOLTAGE))
        assertTrue(types.contains(ControlType.POSITION))
        assertTrue(types.contains(ControlType.VELOCITY))
    }

    @Test
    fun testTalonFXCompatibility() {
        val compatibility = TalonFXCompatibility
        assertTrue(compatibility.supports(ControlType.PWM))
        assertTrue(compatibility.supports(ControlType.VOLTAGE))
        assertTrue(compatibility.supports(ControlType.POSITION))
        assertTrue(compatibility.supports(ControlType.VELOCITY))
        assertEquals(4, compatibility.supportedTypes().size)
    }

    @Test
    fun testPWMTalonSRXCompatibility() {
        val compatibility = PWMTalonSRXCompatibility
        assertTrue(compatibility.supports(ControlType.PWM))
        assertEquals(1, compatibility.supportedTypes().size)
        assertEquals(ControlType.PWM, compatibility.supportedTypes()[0])
    }

    @Test
    fun testPWMSparkMaxCompatibility() {
        val compatibility = PWMSparkMaxCompatibility
        assertTrue(compatibility.supports(ControlType.PWM))
        assertEquals(1, compatibility.supportedTypes().size)
        assertEquals(ControlType.PWM, compatibility.supportedTypes()[0])
    }
}
