package xyz.malefic.frc.pingu

import com.ctre.phoenix6.controls.VoltageOut

/**
 * A class that represents a voltage out request with a pingu cause why not.
 */
object VoltagePingu {
    private var voltageOut = VoltageOut(0.0)

    /**
     * Sets the output of the VoltagePingu.
     *
     * @param output The new output.
     * @return The VoltageOut object representing the output.
     */
    @JvmStatic
    fun setOutput(output: Double): VoltageOut {
        voltageOut = VoltageOut(output)
        return voltageOut
    }
}
