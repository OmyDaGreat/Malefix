# State Machines and Intent Commands

## Overview

Malefix provides a state machine framework that integrates with WPILib's command system to create clear, maintainable robot code.

## Architecture

### Three Layers

1. **State Machines** (Subsystem level)
   - Handle continuous behaviors
   - Run in `periodic()`
   - Maintain subsystem state

2. **Intent Commands** (Coordination level)
   - Coordinate multiple subsystems
   - Set states atomically
   - Manage requirements

3. **Command Composition** (High level)
   - Sequential, parallel, race, deadline
   - PathPlanner integration
   - Autonomous routines

## Quick Start

### 1. Define Subsystem States

```kotlin
enum class ElevatorState(val position: Double) {
    DEFAULT(0.0),
    L1(0.5),
    L2(1.0),
    L3(1.5)
}
```

### 2. Add State Machine to Subsystem

```kotlin
object Elevator : SubsystemBase() {
    private val stateMachine = StateMachine(ElevatorState.DEFAULT)
    
    var state: ElevatorState
        get() = stateMachine.currentState
        set(value) = stateMachine.transitionTo(value)
    
    fun isAtTarget(): Boolean = 
        abs(position - state.position) < 0.1
    
    override fun periodic() {
        // State machine executes continuously
        setPosition(state.position)
    }
}
```

### 3. Create Intent Commands

```kotlin
class PrepareToScoreIntent(val level: ElevatorState) 
    : IntentCommand(Elevator, Arm) {
    
    override fun canExecute() = Intake.hasPiece
    
    override fun onStart() {
        Elevator.state = level
        Arm.state = ArmState.SCORE
    }
    
    override fun isIntentComplete() = 
        Elevator.isAtTarget() && Arm.isAtTarget()
}
```

### 4. Bind to Buttons

```kotlin
controller.a().onTrue(PrepareToScoreIntent(ElevatorState.L2))
```

## Benefits

- ✅ **Clear state representation** - Easy to query "what is the subsystem doing?"
- ✅ **Safe coordination** - Requirements prevent conflicts
- ✅ **Composable** - Works with all WPILib command features
- ✅ **Testable** - Can directly set and verify states
- ✅ **Self-documenting** - Intent names describe purpose

## Examples

See the `examples/` directory for complete robot code examples.
