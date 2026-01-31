package xyz.malefic.frc.pingu.control

import org.littletonrobotics.junction.networktables.LoggedNetworkNumber

/**
 * A data class that wraps magic motion with velocity, acceleration, and jerk.
 *
 * Units depend on the motor's controlled variable:
 * - For rotational motion: velocity in rotations/second, acceleration in rotations/second², jerk in rotations/second³
 * - For linear motion: velocity in meters/second, acceleration in meters/second², jerk in meters/second³
 *
 * @property velocity The velocity (units depend on system - see class docs).
 * @property acceleration The acceleration (units depend on system - see class docs).
 * @property jerk The jerk (units depend on system - see class docs).
 */
data class MagicPingu(
    var velocity: Double = 0.0,
    var acceleration: Double = 0.0,
    var jerk: Double = 0.0,
) {
    /**
     * Sets the velocity of the [MagicPingu].
     *
     * @param velocity The new velocity as a [LoggedNetworkNumber].
     */
    fun setVelocity(velocity: LoggedNetworkNumber) {
        this.velocity = velocity.get()
    }

    /**
     * Sets the acceleration of the [MagicPingu].
     *
     * @param acceleration The new acceleration as a [LoggedNetworkNumber].
     */
    fun setAcceleration(acceleration: LoggedNetworkNumber) {
        this.acceleration = acceleration.get()
    }

    /**
     * Sets the jerk of the [MagicPingu].
     *
     * @param jerk The new jerk as a [LoggedNetworkNumber].
     */
    fun setJerk(jerk: LoggedNetworkNumber) {
        this.jerk = jerk.get()
    }
}
