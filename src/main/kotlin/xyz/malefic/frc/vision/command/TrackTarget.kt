package xyz.malefic.frc.vision.command

import edu.wpi.first.math.controller.PIDController
import edu.wpi.first.wpilibj2.command.Command
import xyz.malefic.frc.swerve.SwerveDrive
import xyz.malefic.frc.vision.Camera
import xyz.malefic.frc.vision.LimeModule
import xyz.malefic.frc.vision.PhotonModule
import kotlin.math.abs

/**
 * Command to continuously track a vision target while allowing manual translation control.
 *
 * This command maintains alignment with a vision target by controlling the robot's rotation
 * while the driver controls forward/backward and strafe movements.
 *
 * ## Usage Example:
 * ```kotlin
 * val trackCommand = TrackTarget(
 *     swerveDrive = drive,
 *     camera = frontCamera,
 *     translationSpeed = { -controller.leftY },
 *     strafeSpeed = { -controller.leftX }
 * ).apply {
 *     configure {
 *         kP = 0.04
 *         maxRotationSpeed = 0.5
 *     }
 * }
 * ```
 *
 * @property swerveDrive The swerve drive subsystem to control.
 * @property camera The camera to use for tracking.
 * @property translationSpeed Supplier for forward/backward speed (-1.0 to 1.0).
 * @property strafeSpeed Supplier for left/right speed (-1.0 to 1.0).
 */
class TrackTarget(
    private val swerveDrive: SwerveDrive,
    private val camera: Camera,
    private val translationSpeed: () -> Double,
    private val strafeSpeed: () -> Double,
) : Command() {
    /**
     * Configuration for target tracking.
     *
     * @property kP Proportional gain for PID controller.
     * @property kI Integral gain for PID controller.
     * @property kD Derivative gain for PID controller.
     * @property maxRotationSpeed Maximum rotation speed (0.0 to 1.0).
     * @property minRotationSpeed Minimum rotation speed to overcome friction.
     * @property deadband Deadband for target offset to avoid oscillation.
     * @property fieldOriented Whether to use field-oriented control for translation.
     * @property stopIfNoTarget Whether to stop the robot if no target is visible.
     */
    data class Config(
        var kP: Double = 0.04,
        var kI: Double = 0.0,
        var kD: Double = 0.004,
        var maxRotationSpeed: Double = 0.5,
        var minRotationSpeed: Double = 0.03,
        var deadband: Double = 1.0,
        var fieldOriented: Boolean = true,
        var stopIfNoTarget: Boolean = false,
    )

    private var config = Config()
    private lateinit var rotationController: PIDController

    init {
        addRequirements(swerveDrive)
    }

    companion object {
        /**
         * Creates a TrackTarget command using a Limelight.
         *
         * @param swerveDrive The swerve drive subsystem.
         * @param limelight The Limelight module.
         * @param translationSpeed Supplier for forward/backward speed.
         * @param strafeSpeed Supplier for left/right speed.
         * @return TrackTarget command configured for the Limelight.
         */
        fun withLimelight(
            swerveDrive: SwerveDrive,
            limelight: LimeModule,
            translationSpeed: () -> Double,
            strafeSpeed: () -> Double,
        ) = TrackTarget(
            swerveDrive = swerveDrive,
            camera = limelight,
            translationSpeed = translationSpeed,
            strafeSpeed = strafeSpeed,
        )

        /**
         * Creates a TrackTarget command using a PhotonVision camera.
         *
         * @param swerveDrive The swerve drive subsystem.
         * @param photonModule The PhotonVision module.
         * @param translationSpeed Supplier for forward/backward speed.
         * @param strafeSpeed Supplier for left/right speed.
         * @return TrackTarget command configured for PhotonVision.
         */
        fun withPhoton(
            swerveDrive: SwerveDrive,
            photonModule: PhotonModule,
            translationSpeed: () -> Double,
            strafeSpeed: () -> Double,
        ) = TrackTarget(
            swerveDrive = swerveDrive,
            camera = photonModule,
            translationSpeed = translationSpeed,
            strafeSpeed = strafeSpeed,
        )
    }

    /**
     * Configures the tracking command using a DSL-style configuration block.
     *
     * ```kotlin
     * trackCommand.configure {
     *     kP = 0.04
     *     maxRotationSpeed = 0.5
     *     deadband = 1.0
     * }
     * ```
     *
     * @param block Configuration block to apply settings.
     * @return This command for method chaining.
     */
    fun configure(block: Config.() -> Unit): TrackTarget =
        apply {
            config.apply(block)
        }

    override fun initialize() {
        rotationController =
            PIDController(config.kP, config.kI, config.kD).apply {
                enableContinuousInput(-180.0, 180.0)
            }
    }

    override fun execute() {
        val horizontalOffset = getHorizontalOffset()

        if (horizontalOffset == null) {
            if (config.stopIfNoTarget) {
                swerveDrive.stop()
            } else {
                // No target visible, allow manual control
                swerveDrive.drive(
                    translationSpeed(),
                    strafeSpeed(),
                    0.0,
                    config.fieldOriented,
                )
            }
            return
        }

        // Apply deadband to avoid oscillation
        if (abs(horizontalOffset) < config.deadband) {
            swerveDrive.drive(
                translationSpeed(),
                strafeSpeed(),
                0.0,
                config.fieldOriented,
            )
            return
        }

        // Calculate rotation speed from PID controller
        var rotationSpeed = rotationController.calculate(horizontalOffset, 0.0)

        // Clamp and apply minimum speed
        rotationSpeed =
            rotationSpeed
                .coerceIn(-config.maxRotationSpeed, config.maxRotationSpeed)
                .let { speed ->
                    if (abs(speed) < config.minRotationSpeed && abs(horizontalOffset) > config.deadband) {
                        if (speed > 0) config.minRotationSpeed else -config.minRotationSpeed
                    } else {
                        speed
                    }
                }

        // Drive with rotation control
        swerveDrive.drive(
            translationSpeed(),
            strafeSpeed(),
            rotationSpeed,
            config.fieldOriented,
        )
    }

    override fun end(interrupted: Boolean) {
        swerveDrive.stop()
    }

    /**
     * This command runs indefinitely until interrupted.
     */
    override fun isFinished(): Boolean = false

    /**
     * Gets the horizontal offset to the target in degrees.
     *
     * @return Horizontal offset in degrees, or null if no target is visible.
     */
    private fun getHorizontalOffset(): Double? = camera.getTargetHorizontalOffset()
}
