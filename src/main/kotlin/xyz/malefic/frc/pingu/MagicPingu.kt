package xyz.malefic.frc.pingu

import org.littletonrobotics.junction.networktables.LoggedNetworkNumber

/**
 * A data class that wraps magic motion with velocity, acceleration, and jerk.
 *
 * @property velocity The velocity of the [MagicPingu].
 * @property acceleration The acceleration of the [MagicPingu].
 * @property jerk The jerk of the [MagicPingu].
 */
data class MagicPingu(
    var velocity: Double,
    var acceleration: Double,
    var jerk: Double,
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
