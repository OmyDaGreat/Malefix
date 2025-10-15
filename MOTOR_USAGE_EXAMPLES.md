# Motor Usage Examples

This document provides examples of how to use the new interface-based motor system in Malefix.

## Overview

The new Mongu system is now an interface that is implemented by motor-specific classes. Each motor class extends its corresponding hardware class and implements the Mongu interface, providing:
- Direct access to all motor methods through inheritance
- Direct access to control classes for advanced control
- Common interface for polymorphic usage
- Type-safe configuration through DSL-style blocks

## TonguFX (TalonFX)

### Basic Usage

```kotlin
import xyz.malefic.frc.pingu.motor.talonfx.TonguFX
import com.ctre.phoenix6.signals.InvertedValue
import com.ctre.phoenix6.signals.NeutralModeValue

// Create a TalonFX motor with configuration
val motor = TonguFX(1) {
    inverted = InvertedValue.CounterClockwise_Positive
    neutralMode = NeutralModeValue.Brake
    pingu.p = 0.1
    pingu.i = 0.0
    pingu.d = 0.0
}

// Move the motor using PWM control
motor.move(0.5)  // 50% forward

// Stop the motor
motor.stopMotor()
// or use the operator
!motor
```

### Advanced Control with Direct Access

```kotlin
// Direct access to control classes
val motor = TonguFX(1) {
    pingu.p = 0.1
    currentLimits = 40.0 to 60.0
}

// Use position control directly
motor.setControl(motor.positionControl.withPosition(10.0))

// Use velocity control directly
motor.setControl(motor.velocityControl.withVelocity(100.0))

// Access motor properties directly (inherited from TalonFX)
val currentPosition = motor.position.valueAsDouble
val currentVelocity = motor.velocity.valueAsDouble
val deviceId = motor.deviceID
```

### Extension Properties

```kotlin
import xyz.malefic.frc.pingu.motor.talonfx.pingu
import xyz.malefic.frc.pingu.motor.talonfx.resetPosition

// Access PID configuration
val pidConfig = motor.pingu

// Reset position
motor.resetPosition(0.0)
```

### Reconfiguration

```kotlin
// Reconfigure the motor after creation
motor.configure {
    pingu.p = 0.15
    softLimits = 100.0 to -100.0
}
```

## SparkuMax (PWMSparkMax)

### Basic Usage

```kotlin
import xyz.malefic.frc.pingu.motor.cansparkmax.SparkuMax

// Create a PWMSparkMax motor
val motor = SparkuMax(0) {
    inverted = true
    deadbandElimination = false
}

// Move the motor using PWM control
motor.move(0.75)  // 75% forward

// Stop the motor
motor.stopMotor()
```

### Direct Access to Motor Methods

```kotlin
// All PWMSparkMax methods are directly accessible
motor.set(0.5)
motor.setVoltage(6.0)
motor.disable()

// Access motor properties
val channel = motor.channel
val isInverted = motor.inverted
```

### Extension Properties

```kotlin
import xyz.malefic.frc.pingu.motor.cansparkmax.pwm

// Get current PWM value
val currentPWM = motor.pwm
```

## TalonuSRX (PWMTalonSRX)

### Basic Usage

```kotlin
import xyz.malefic.frc.pingu.motor.pwmtalonsrx.TalonuSRX

// Create a PWMTalonSRX motor
val motor = TalonuSRX(0) {
    inverted = false
    deadbandElimination = true
}

// Move the motor
motor.move(0.5)

// Stop the motor
motor.stopMotor()
```

### Direct Access to Motor Methods

```kotlin
// All PWMTalonSRX methods are directly accessible
motor.set(0.5)
motor.setVoltage(6.0)
motor.disable()

// Access motor properties
val channel = motor.channel
val isInverted = motor.inverted
```

### Extension Properties

```kotlin
import xyz.malefic.frc.pingu.motor.pwmtalonsrx.pwm

// Get current PWM value
val currentPWM = motor.pwm
```

## Polymorphic Usage

The Mongu interface allows you to use different motor types polymorphically:

```kotlin
import xyz.malefic.frc.pingu.motor.Mongu
import xyz.malefic.frc.pingu.motor.MonguConfig

// Function that works with any motor implementing Mongu
fun controlMotor(motor: Mongu<*>, speed: Double) {
    motor.move(speed)
}

// Use with different motor types
val talonFX = TonguFX(1)
val sparkMax = SparkuMax(0)
val talonSRX = TalonuSRX(1)

controlMotor(talonFX, 0.5)
controlMotor(sparkMax, 0.5)
controlMotor(talonSRX, 0.5)
```

## Migration from Old Mongu Wrapper

### Old Way (Wrapper)
```kotlin
import xyz.malefic.frc.pingu.motor.Mongu
import com.ctre.phoenix6.hardware.TalonFX
import xyz.malefic.frc.pingu.motor.ControlType

// Old wrapper approach
val motor = Mongu(TalonFX(1), control = ControlType.PWM) {
    this as TalonFXConfig
    pingu.p = 0.1
}
motor.move(0.5)

// Access underlying motor
val deviceId = motor.motor.deviceID
```

### New Way (Interface)
```kotlin
import xyz.malefic.frc.pingu.motor.talonfx.TonguFX

// New interface approach
val motor = TonguFX(1) {
    pingu.p = 0.1
}
motor.move(0.5)

// Direct access to motor properties (no .motor needed)
val deviceId = motor.deviceID
```

## Key Differences

1. **Direct Inheritance**: Motor classes extend the actual hardware classes, so all methods are directly accessible
2. **No Wrapper Layer**: No need to access `.motor` to get to the underlying motor
3. **Direct Control Access**: TonguFX provides direct access to `positionControl` and `velocityControl` objects
4. **Type Safety**: Configuration blocks are strongly typed to the specific motor config
5. **Simplified API**: Cleaner, more intuitive API that feels like working directly with the motor

## Benefits

1. **Better IDE Support**: Full auto-completion for all motor methods
2. **No Wrapper Overhead**: Direct access to motor without intermediate layer
3. **More Flexible**: Can use motors polymorphically via Mongu interface or use specific motor features
4. **Clearer Intent**: Motor-specific classes make it clear what type of motor you're working with
5. **Advanced Control**: Direct access to control objects for advanced use cases
