package xyz.malefic.frc.swerve.command

import edu.wpi.first.math.MathUtil
import edu.wpi.first.math.filter.SlewRateLimiter
import edu.wpi.first.wpilibj2.command.Command
import xyz.malefic.frc.swerve.SwerveDrive
import kotlin.math.absoluteValue
import kotlin.math.pow
import kotlin.math.sign

/**
 * Command for teleop swerve drive control with advanced input processing.
 *
 * Provides features like:
 * - Deadband filtering
 * - Input squaring/cubing for finer control
 * - Slew rate limiting for smooth acceleration
 * - Field-oriented or robot-oriented driving
 *
 * ## Usage Example:
 * ```kotlin
 * val driveCommand = TeleopSwerve(
 *     swerveDrive = drive,
 *     xSupplier = { controller.leftY },
 *     ySupplier = { controller.leftX },
 *     rotSupplier = { controller.rightX },
 *     fieldOriented = true
 * ).apply {
 *     configure {
 *         deadband = 0.05
 *         inputExponent = 2.0
 *         enableSlewRateLimiting = true
 *         translationSlewRate = 3.0
 *         rotationSlewRate = 3.0
 *     }
 * }
 * ```
 *
 * @property swerveDrive The swerve drive subsystem.
 * @property xSupplier Supplier for forward/backward input (-1.0 to 1.0).
 * @property ySupplier Supplier for left/right input (-1.0 to 1.0).
 * @property rotSupplier Supplier for rotation input (-1.0 to 1.0).
 * @property fieldOriented Whether to use field-oriented control.
 */
class TeleopSwerve(
    private val swerveDrive: SwerveDrive,
    private val xSupplier: () -> Double,
    private val ySupplier: () -> Double,
    private val rotSupplier: () -> Double,
    private val fieldOriented: Boolean = true,
) : Command() {
    /**
     * Configuration for teleop swerve control.
     *
     * @property deadband Deadband for controller inputs (0.0 to 1.0).
     * @property inputExponent Exponent for input shaping (1.0 = linear, 2.0 = squared, etc.).
     * @property enableSlewRateLimiting Whether to enable slew rate limiting.
     * @property translationSlewRate Slew rate limit for translation in units per second.
     * @property rotationSlewRate Slew rate limit for rotation in units per second.
     */
    data class Config(
        var deadband: Double = 0.05,
        var inputExponent: Double = 2.0,
        var enableSlewRateLimiting: Boolean = true,
        var translationSlewRate: Double = 3.0,
        var rotationSlewRate: Double = 3.0,
    )

    private var config = Config()

    private lateinit var xLimiter: SlewRateLimiter
    private lateinit var yLimiter: SlewRateLimiter
    private lateinit var rotLimiter: SlewRateLimiter

    init {
        addRequirements(swerveDrive)
        initializeLimiters()
    }

    private fun initializeLimiters() {
        xLimiter = SlewRateLimiter(config.translationSlewRate)
        yLimiter = SlewRateLimiter(config.translationSlewRate)
        rotLimiter = SlewRateLimiter(config.rotationSlewRate)
    }

    /**
     * Configures the teleop swerve command using a DSL-style configuration block.
     *
     * ```kotlin
     * teleopCommand.configure {
     *     deadband = 0.05
     *     inputExponent = 2.0
     *     enableSlewRateLimiting = true
     * }
     * ```
     *
     * @param block Configuration block to apply settings.
     */
    fun configure(block: Config.() -> Unit) {
        config.apply(block)
        initializeLimiters()
    }

    override fun initialize() {
        // Reset limiters when command starts
        xLimiter.reset(0.0)
        yLimiter.reset(0.0)
        rotLimiter.reset(0.0)
    }

    override fun execute() {
        val xInput = processInput(xSupplier())
        val yInput = processInput(ySupplier())
        val rotInput = processInput(rotSupplier())

        val xSpeed =
            if (config.enableSlewRateLimiting) {
                xLimiter.calculate(xInput)
            } else {
                xInput
            }

        val ySpeed =
            if (config.enableSlewRateLimiting) {
                yLimiter.calculate(yInput)
            } else {
                yInput
            }

        val rotSpeed =
            if (config.enableSlewRateLimiting) {
                rotLimiter.calculate(rotInput)
            } else {
                rotInput
            }

        swerveDrive.drive(xSpeed, ySpeed, rotSpeed, fieldOriented)
    }

    override fun end(interrupted: Boolean) {
        swerveDrive.stop()
    }

    private fun processInput(input: Double): Double {
        val processed = MathUtil.applyDeadband(input, config.deadband)

        if (config.inputExponent != 1.0) {
            return processed.absoluteValue.pow(config.inputExponent) * processed.sign
        }

        return processed
    }
}
