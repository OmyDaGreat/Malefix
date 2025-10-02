package xyz.malefic.frc.pingu.control

import edu.wpi.first.math.controller.PIDController
import org.littletonrobotics.junction.networktables.LoggedNetworkNumber

/**
 * A class that represents a PID controller with network logging capabilities.
 *
 * @property p The proportional gain as a [LoggedNetworkNumber].
 * @property i The integral gain as a [LoggedNetworkNumber].
 * @property d The derivative gain as a [LoggedNetworkNumber].
 * @property v The velocity gain as a [LoggedNetworkNumber].
 * @property s The static gain as a [LoggedNetworkNumber].
 * @property g The gravity gain as a [LoggedNetworkNumber].
 * @property pingu The underlying [Pingu] instance using the current gains.
 * @throws NullPointerException if [v], [s], or [g] are not set and are then accessed.
 */
class NetworkPingu(
    var p: LoggedNetworkNumber,
    var i: LoggedNetworkNumber,
    var d: LoggedNetworkNumber,
    var v: LoggedNetworkNumber? = null,
    var s: LoggedNetworkNumber? = null,
    var g: LoggedNetworkNumber? = null,
) : PIDController(p.get(), i.get(), d.get()) {
    /**
     * The underlying [Pingu] instance using the current gains.
     */
    val pingu: Pingu = Pingu(p.get(), i.get(), d.get(), v?.get() ?: 0.0, s?.get() ?: 0.0, g?.get() ?: 0.0)
}
