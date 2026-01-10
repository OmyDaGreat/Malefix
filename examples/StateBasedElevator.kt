package examples

import edu.wpi.first.wpilibj2.command.Command
import edu.wpi.first.wpilibj2.command.SubsystemBase
import xyz.malefic.frc.statemachine.IntentCommand
import xyz.malefic.frc.statemachine.StateMachine
import kotlin.math.abs

// State definition
enum class ElevatorState(val position: Double) {
    DEFAULT(0.0),
    L1(0.5),
    L2(1.0),
    L3(1.5),
    L4(2.0),
}

// Subsystem with state machine
object Elevator : SubsystemBase() {
    private val stateMachine = StateMachine(ElevatorState.DEFAULT)
    private var position: Double = 0.0 // Simulated position

    var state: ElevatorState
        get() = stateMachine.currentState
        set(value) = stateMachine.transitionTo(value)

    fun isAtTarget(tolerance: Double = 0.1): Boolean = abs(position - state.position) < tolerance

    override fun periodic() {
        // State machine executes: move toward target
        val target = state.position
        if (position < target) {
            position += 0.05 // Simulate movement
        } else if (position > target) {
            position -= 0.05
        }
    }
}

// Intent command for coordination
class PrepareToScoreIntent(
    val level: ElevatorState,
) : IntentCommand(Elevator) {
    init {
        name = "Prepare Score ${level.name}"
    }

    override fun onStart() {
        Elevator.state = level
    }

    override fun isIntentComplete() = Elevator.isAtTarget()
}

// Usage examples
fun exampleUsage() {
    // Simple button binding
    val command: Command = PrepareToScoreIntent(ElevatorState.L2)

    // Command composition
    val sequence =
        PrepareToScoreIntent(ElevatorState.L2)
            .andThen(Elevator.runOnce { println("Score!") })

    // Conditional
    val smartScore =
        if (Elevator.state == ElevatorState.DEFAULT) {
            PrepareToScoreIntent(ElevatorState.L2)
        } else {
            PrepareToScoreIntent(ElevatorState.L4)
        }
}
