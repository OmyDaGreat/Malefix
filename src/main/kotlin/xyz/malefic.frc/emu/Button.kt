package xyz.malefic.frc.emu

import edu.wpi.first.wpilibj.XboxController

/**
 * Enum class representing the buttons on a joystick or game controller.
 */
enum class Button(
    val checkPressed: (XboxController) -> Boolean,
) {
    A({ it.aButtonPressed }),

    B({ it.bButtonPressed }),

    X({ it.xButtonPressed }),

    Y({ it.yButtonPressed }),

    START({ it.startButtonPressed }),

    LEFT_BUMPER({ it.leftBumperButtonPressed }),

    RIGHT_BUMPER({ it.rightBumperButtonPressed }),

    BACK({ it.backButtonPressed }),

    LEFT_STICK({ it.leftStickButtonPressed }),

    RIGHT_STICK({ it.rightStickButtonPressed }),

    LEFT_TRIGGER({ it.leftTriggerAxis > 0.5 }),

    RIGHT_TRIGGER({ it.rightTriggerAxis > 0.5 }),
}
