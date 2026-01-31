package xyz.malefic.frc.vision.command

import edu.wpi.first.math.controller.PIDController
import edu.wpi.first.units.Units
import edu.wpi.first.wpilibj2.command.Command
import xyz.malefic.frc.swerve.SwerveDrive
import xyz.malefic.frc.vision.Camera
import xyz.malefic.frc.vision.LimeModule
import xyz.malefic.frc.vision.PhotonModule
import kotlin.math.abs

/**
 * Command to align the robot to a vision target using rotational control.
 *
 * This command uses PID control to rotate the robot until it is aligned with a detected
 * target from any camera implementing the [Camera] interface.
 *
 * ## Usage Example:
 * ```kotlin
 * val alignCommand = AlignToTarget(
 *     swerveDrive = drive,
 *     camera = frontCamera,
 *     translationSpeed = { 0.0 },
 *     strafeSpeed = { 0.0 }
 * ).apply {
 *     configure {
 *         kP = 0.05
 *         toleranceDegrees = 2.0
 *     }
 * }
 * ```
 *
 * @property swerveDrive The swerve drive subsystem to control.
 * @property camera The camera to use for alignment.
 * @property translationSpeed Supplier for forward/backward speed (-1.0 to 1.0).
 * @property strafeSpeed Supplier for left/right speed (-1.0 to 1.0).
 */
class AlignToTarget(
    private val swerveDrive: SwerveDrive,
    private val camera: Camera,
    private val translationSpeed: () -> Double = { 0.0 },
    private val strafeSpeed: () -> Double = { 0.0 },
) : Command() {
    /**
     * Configuration for target alignment.
     *
     * @property kP Proportional gain for PID controller.
     * @property kI Integral gain for PID controller.
     * @property kD Derivative gain for PID controller.
     * @property toleranceDegrees Tolerance in degrees for considering alignment complete.
     * @property maxRotationSpeed Maximum rotation speed (0.0 to 1.0).
     * @property minRotationSpeed Minimum rotation speed to overcome friction.
     * @property fieldOriented Whether to use field-oriented control for translation.
     */
    data class Config(
        var kP: Double = 0.05,
        var kI: Double = 0.0,
        var kD: Double = 0.005,
        var toleranceDegrees: Double = 2.0,
        var maxRotationSpeed: Double = 0.4,
        var minRotationSpeed: Double = 0.05,
        var fieldOriented: Boolean = true,
    )

    private var config = Config()
    private lateinit var rotationController: PIDController

    init {
        addRequirements(swerveDrive)
    }

    companion object {
        /**
         * Creates an AlignToTarget command using a Limelight.
         *
         * @param swerveDrive The swerve drive subsystem.
         * @param limelight The Limelight module.
         * @param translationSpeed Supplier for forward/backward speed.
         * @param strafeSpeed Supplier for left/right speed.
         * @return AlignToTarget command configured for the Limelight.
         */
        fun withLimelight(
            swerveDrive: SwerveDrive,
            limelight: LimeModule,
            translationSpeed: () -> Double = { 0.0 },
            strafeSpeed: () -> Double = { 0.0 },
        ) = AlignToTarget(
            swerveDrive = swerveDrive,
            camera = limelight,
            translationSpeed = translationSpeed,
            strafeSpeed = strafeSpeed,
        )

        /**
         * Creates an AlignToTarget command using a PhotonVision camera.
         *
         * @param swerveDrive The swerve drive subsystem.
         * @param photonModule The PhotonVision module.
         * @param translationSpeed Supplier for forward/backward speed.
         * @param strafeSpeed Supplier for left/right speed.
         * @return AlignToTarget command configured for PhotonVision.
         */
        fun withPhoton(
            swerveDrive: SwerveDrive,
            photonModule: PhotonModule,
            translationSpeed: () -> Double = { 0.0 },
            strafeSpeed: () -> Double = { 0.0 },
        ) = AlignToTarget(
            swerveDrive = swerveDrive,
            camera = photonModule,
            translationSpeed = translationSpeed,
            strafeSpeed = strafeSpeed,
        )
    }

    /**
     * Configures the alignment command using a DSL-style configuration block.
     *
     * ```kotlin
     * alignCommand.configure {
     *     kP = 0.05
     *     toleranceDegrees = 2.0
     *     maxRotationSpeed = 0.3
     * }
     * ```
     *
     * @param block Configuration block to apply settings.
     * @return This command for method chaining.
     */
    fun configure(block: Config.() -> Unit): AlignToTarget =
        apply {
            config.apply(block)
        }

    override fun initialize() {
        rotationController =
            PIDController(config.kP, config.kI, config.kD).apply {
                setTolerance(config.toleranceDegrees)
                enableContinuousInput(-180.0, 180.0)
            }
    }

    override fun execute() {
        val horizontalOffset = getHorizontalOffset()

        if (horizontalOffset == null) {
            // No target visible, stop rotation but allow translation
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
                    if (abs(speed) < config.minRotationSpeed && abs(horizontalOffset) > config.toleranceDegrees) {
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

    override fun isFinished(): Boolean {
        val horizontalOffset = getHorizontalOffset() ?: return false
        return rotationController.atSetpoint() && abs(horizontalOffset) < config.toleranceDegrees
    }

    /**
     * Gets the horizontal offset to the target in degrees.
     *
     * @return Horizontal offset in degrees, or null if no target is visible.
     */
    private fun getHorizontalOffset(): Double? = camera.getTargetHorizontalOffset()?.`in`(Units.Degrees)
}
