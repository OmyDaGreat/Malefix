package xyz.malefic.frc.extension

import edu.wpi.first.math.util.Units.inchesToMeters
import edu.wpi.first.math.util.Units.metersToInches

/**
 * Extension property to convert a value in inches to meters.
 *
 * @receiver [Double] The value in inches.
 * @return [Double] The value converted to meters.
 */
val Double.inchesToMeters: Double
    get() = inchesToMeters(this)

/**
 * Extension property to convert a value in meters to inches.
 *
 * @receiver [Double] The value in meters.
 * @return [Double] The value converted to inches.
 */
val Double.metersToInches: Double
    get() = metersToInches(this)
