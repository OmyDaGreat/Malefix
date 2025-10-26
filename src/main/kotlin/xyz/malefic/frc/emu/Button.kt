package xyz.malefic.frc.emu

import edu.wpi.first.wpilibj.XboxController

/**
 * Enum class representing the buttons on a joystick or game controller.
 *
 * Each [Button] provides lambdas to check if it is pressed or released on an [XboxController].
 *
 * @property checkPressed Lambda to check if the button is pressed on the [XboxController].
 * @property checkReleased Lambda to check if the button is released on the [XboxController].
 */
enum class Button(
    val checkPressed: (XboxController) -> Boolean,
    val checkReleased: (XboxController) -> Boolean,
) {
    A({ it.aButton }, { !it.aButton }),

    B({ it.bButton }, { !it.bButton }),

    X({ it.xButton }, { !it.xButton }),

    Y({ it.yButton }, { !it.yButton }),

    START({ it.startButton }, { !it.startButton }),

    LEFT_BUMPER({ it.leftBumperButton }, { !it.leftBumperButton }),

    RIGHT_BUMPER({ it.rightBumperButton }, { !it.rightBumperButton }),

    BACK({ it.backButton }, { !it.backButton }),

    LEFT_STICK({ it.leftStickButton }, { !it.leftStickButton }),

    RIGHT_STICK({ it.rightStickButton }, { !it.rightStickButton }),

    LEFT_TRIGGER({ it.leftTriggerAxis > 0.5 }, { it.leftTriggerAxis <= 0.5 }),

    RIGHT_TRIGGER({ it.rightTriggerAxis > 0.5 }, { it.rightTriggerAxis <= 0.5 }),

    DPAD_UP({ (it.pov in 315..360) || (it.pov in 0..45) }, { !((it.pov in 315..360) || (it.pov in 0..45)) }),

    DPAD_RIGHT({ it.pov in 45..135 }, { it.pov !in 45..135 }),

    DPAD_DOWN({ it.pov in 135..225 }, { it.pov !in 135..225 }),

    DPAD_LEFT({ it.pov in 225..315 }, { it.pov !in 225..315 }),

    DPAD_NONE({ it.pov == -1 }, { it.pov != -1 }),
}
