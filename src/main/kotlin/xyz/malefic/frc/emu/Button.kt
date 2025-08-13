package xyz.malefic.frc.emu

import edu.wpi.first.wpilibj.XboxController

/**
 * Enum class representing the buttons on a joystick or game controller.
 */
enum class Button(
    val checkPressed: (XboxController) -> Boolean,
    val checkReleased: (XboxController) -> Boolean,
) {
    A({ it.aButtonPressed }, { it.aButtonReleased }),

    B({ it.bButtonPressed }, { it.bButtonReleased }),

    X({ it.xButtonPressed }, { it.xButtonReleased }),

    Y({ it.yButtonPressed }, { it.yButtonReleased }),

    START({ it.startButtonPressed }, { it.startButtonReleased }),

    LEFT_BUMPER({ it.leftBumperButtonPressed }, { it.leftBumperButtonReleased }),

    RIGHT_BUMPER({ it.rightBumperButtonPressed }),

    BACK({ it.backButtonPressed }),

    LEFT_STICK({ it.leftStickButtonPressed }),

    RIGHT_STICK({ it.rightStickButtonPressed }),

    LEFT_TRIGGER({ it.leftTriggerAxis > 0.5 }),

    RIGHT_TRIGGER({ it.rightTriggerAxis > 0.5 }),

    DPAD_UP({ it.pov == 0 }),

    DPAD_RIGHT({ it.pov == 90 }),

    DPAD_DOWN({ it.pov == 180 }),

    DPAD_LEFT({ it.pov == 270 }),
}
