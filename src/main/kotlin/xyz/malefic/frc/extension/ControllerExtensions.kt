package xyz.malefic.frc.extension

import edu.wpi.first.wpilibj.XboxController
import kotlin.math.abs

/**
 * Gets the position of the left stick based on the input from the controller.
 *
 * @receiver [XboxController] The controller.
 * @return [Pair]<Double, Double> The coordinate representing the position of the left stick. The first element is the x-coordinate, and
 * the second element is the y-coordinate.
 */
fun XboxController.leftStickPosition(
    xDeadzone: Double,
    yDeadzone: Double,
): Pair<Double, Double> {
    val x = if (abs(leftX) < xDeadzone) 0.0 else leftX
    val y = if (abs(leftY) < yDeadzone) 0.0 else leftY
    return Pair(x, y)
}

/**
 * Gets the position of the right stick based on the input from the controller.
 *
 * @receiver [XboxController] The controller.
 * @return [Pair]<Double, Double> The coordinate representing the position of the right stick. The first element is the x-coordinate, and
 * the second element is the y-coordinate.
 */
fun XboxController.rightStickPosition(
    xDeadzone: Double,
    yDeadzone: Double,
): Pair<Double, Double> {
    val x = if (abs(rightX) < xDeadzone) 0.0 else rightX
    val y = if (abs(rightY) < yDeadzone) 0.0 else rightY
    return Pair(x, y)
}
