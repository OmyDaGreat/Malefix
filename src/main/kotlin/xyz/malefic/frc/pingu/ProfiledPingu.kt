package xyz.malefic.frc.pingu

import edu.wpi.first.math.controller.ProfiledPIDController
import edu.wpi.first.math.trajectory.TrapezoidProfile
import org.littletonrobotics.junction.networktables.LoggedNetworkNumber

/**
 * Data class representing a [ProfiledPingu] with PID controller parameters and profile constraints.
 *
 * @property p Proportional gain.
 * @property i Integral gain.
 * @property d Derivative gain.
 * @property v Optional velocity feedforward term.
 * @property s Optional static feedforward term.
 * @property g Optional gravity feedforward term.
 * @property profile Constraints for the trapezoidal profile.
 */
data class ProfiledPingu(
    var p: Double,
    var i: Double,
    var d: Double,
    var v: Double = 0.0,
    var s: Double = 0.0,
    var g: Double = 0.0,
    val profile: TrapezoidProfile.Constraints,
) {
    /**
     * Gets the [ProfiledPIDController] instance with the current PID parameters and profile constraints.
     */
    val profiledPIDController
        get() = ProfiledPIDController(p, i, d, profile)

    /**
     * Gets a [Pingu] instance with the current PID and feedforward parameters.
     */
    val pingu
        get() = Pingu(p, i, d, v, s, g)

    /**
     * Sets the PID parameters from the given [ProfiledPIDController] instance.
     *
     * @param profiledPIDController The ProfiledPIDController instance to copy parameters from.
     */
    fun setPID(profiledPIDController: ProfiledPIDController) {
        p = profiledPIDController.p
        i = profiledPIDController.i
        d = profiledPIDController.d
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
