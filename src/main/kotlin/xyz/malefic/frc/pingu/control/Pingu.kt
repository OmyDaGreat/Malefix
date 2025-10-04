package xyz.malefic.frc.pingu.control

import edu.wpi.first.math.controller.PIDController
import org.littletonrobotics.junction.networktables.LoggedNetworkNumber

/**
 * Data class representing a [Pingu] with PID controller parameters.
 *
 * @property p Proportional gain.
 * @property i Integral gain.
 * @property d Derivative gain.
 * @property v Velocity feedforward term.
 * @property s Static feedforward term.
 * @property g Gravity feedforward term.
 * @property pidController The [PIDController] instance with the current PID parameters.
 */
data class Pingu(
    var p: Double = 0.0,
    var i: Double = 0.0,
    var d: Double = 0.0,
    var v: Double = 0.0,
    var s: Double = 0.0,
    var g: Double = 0.0,
) {
    /**
     * Gets the [PIDController] instance with the current PID parameters.
     */
    val pidController
        get() = PIDController(p, i, d)

    /**
     * Sets the PID parameters from the given [PIDController] instance.
     *
     * @param pidController The [PIDController] instance to copy parameters from.
     */
    fun setPID(pidController: PIDController) {
        p = pidController.p
        i = pidController.i
        d = pidController.d
    }

    /**
     * Sets the proportional gain from a [LoggedNetworkNumber].
     *
     * @param p The [LoggedNetworkNumber] instance to get the value from.
     */
    fun setP(p: LoggedNetworkNumber) {
        this.p = p.get()
    }

    /**
     * Sets the integral gain from a [LoggedNetworkNumber].
     *
     * @param i The [LoggedNetworkNumber] instance to get the value from.
     */
    fun setI(i: LoggedNetworkNumber) {
        this.i = i.get()
    }

    /**
     * Sets the derivative gain from a [LoggedNetworkNumber].
     *
     * @param d The [LoggedNetworkNumber] instance to get the value from.
     */
    fun setD(d: LoggedNetworkNumber) {
        this.d = d.get()
    }

    /**
     * Sets the velocity feedforward term from a [LoggedNetworkNumber].
     *
     * @param v The [LoggedNetworkNumber] instance to get the value from.
     */
    fun setV(v: LoggedNetworkNumber) {
        this.v = v.get()
    }

    /**
     * Sets the static feedforward term from a [LoggedNetworkNumber].
     *
     * @param s The [LoggedNetworkNumber] instance to get the value from.
     */
    fun setS(s: LoggedNetworkNumber) {
        this.s = s.get()
    }

    /**
     * Sets the gravity feedforward term from a [LoggedNetworkNumber].
     *
     * @param g The [LoggedNetworkNumber] instance to get the value from.
     */
    fun setG(g: LoggedNetworkNumber) {
        this.g = g.get()
    }
}
