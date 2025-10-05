package xyz.malefic.frc.pingu.control

import com.ctre.phoenix6.controls.VoltageOut

/**
 * A class that represents a [VoltageOut] request with a [VoltagePingu].
 */
@Deprecated("Use setVoltage function from TalonFX or similar motor classes instead", ReplaceWith("setVoltage"))
object VoltagePingu {
    private var voltageOut = VoltageOut(0.0)

    /**
     * Sets the output of the [VoltagePingu].
     *
     * @param output The new output voltage.
     * @return The [VoltageOut] object representing the output.
     */
    @JvmStatic
    fun setOutput(output: Double): VoltageOut {
        voltageOut = VoltageOut(output)
        return voltageOut
    }
}
