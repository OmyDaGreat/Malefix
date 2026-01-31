package xyz.malefic.frc.extension

import com.ctre.phoenix6.configs.TalonFXConfiguration
import com.ctre.phoenix6.hardware.TalonFX
import xyz.malefic.frc.pingu.control.Pingu

/**
 * Extension function to set the Pingu values of a [TalonFXConfiguration] using a [Pingu] object.
 *
 * @receiver [TalonFXConfiguration] The [TalonFX] configuration to set the values for.
 * @param pingu [Pingu] The [Pingu] object containing the PID values.
 */
fun TalonFXConfiguration.setPingu(pingu: Pingu) =
    pingu.apply {
        Slot0.kP = p
        Slot0.kI = i
        Slot0.kD = d
        Slot0.kV = v
        Slot0.kS = s
        Slot0.kG = g
    }

/**
 * Extension function to get the [Pingu] values from a [TalonFXConfiguration].
 *
 * @return [Pingu] The [Pingu] object containing the PID values.
 */
fun TalonFXConfiguration.getPingu(): Pingu =
    Pingu(
        p = Slot0.kP,
        i = Slot0.kI,
        d = Slot0.kD,
        v = Slot0.kV,
        s = Slot0.kS,
        g = Slot0.kG,
    )
