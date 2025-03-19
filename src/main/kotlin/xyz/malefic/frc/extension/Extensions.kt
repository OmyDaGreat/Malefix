package xyz.malefic.frc.extension

import com.ctre.phoenix6.configs.TalonFXConfiguration
import edu.wpi.first.math.geometry.Pose2d
import edu.wpi.first.math.geometry.Rotation2d
import edu.wpi.first.math.geometry.Rotation3d
import org.photonvision.EstimatedRobotPose
import org.photonvision.targeting.PhotonPipelineResult
import xyz.malefic.frc.pingu.NetworkPingu
import xyz.malefic.frc.pingu.Pingu
import xyz.malefic.frc.sub.PhotonModule
import java.util.Optional

/**
 * Extension function for a list of PhotonModule objects to get the best PhotonPipelineResult.
 *
 * This function iterates through each PhotonModule in the list, retrieves the latest result,
 * and checks if it has targets. If it does, it compares the pose ambiguity of the target
 * with the current best ambiguity. If the current target's ambiguity is lower, it updates
 * the best result.
 *
 * @receiver List<PhotonModule> The list of PhotonModule objects to search through.
 * @return List<Pair<PhotonModule, PhotonPipelineResult>> The list of PhotonModule and PhotonPipelineResult pairs ordered by pose ambiguity.
 */
fun List<PhotonModule>.getDecentResultPairs(): List<Pair<PhotonModule, PhotonPipelineResult>> =
    this
        .mapNotNull { module ->
            module.allUnreadResults
                .getOrNull(0)
                ?.takeIf {
                    it.hasTargets() // && module.currentStdDevs.normF() < 0.9
                }?.let { module to it }
        }.sortedBy { it.second.bestTarget.poseAmbiguity }

/**
 * Extension function for a list of Pair<PhotonModule, PhotonPipelineResult> objects to check if any have targets.
 *
 * This function iterates through each pair in the list and checks if the PhotonPipelineResult has targets.
 *
 * @receiver List<Pair<PhotonModule, PhotonPipelineResult>> The list of pairs to check.
 * @return Boolean True if any pair has targets, false otherwise.
 */
fun List<Pair<PhotonModule, PhotonPipelineResult>>.hasTargets(): Boolean = this.any { it.second.hasTargets() }

/**
 * Extension function for a Pair of PhotonModule and PhotonPipelineResult to get estimated poses.
 *
 * This function sets the reference pose for the pose estimator of the PhotonModule and updates it
 * with the PhotonPipelineResult. If an estimated robot pose is present, it adds it to the list of poses.
 *
 * @receiver Pair<PhotonModule, PhotonPipelineResult> The pair of PhotonModule and PhotonPipelineResult.
 * @param prevEstimatedRobotPose Pose2d? The previous estimated robot pose to set as reference.
 * @return List<EstimatedRobotPose> The list of estimated robot poses.
 */
fun Pair<PhotonModule, PhotonPipelineResult>.getEstimatedPose(prevEstimatedRobotPose: Pose2d?): EstimatedRobotPose? {
    first.poseEstimator.apply {
        setReferencePose(prevEstimatedRobotPose)
        return update(second).orElse(null)
    }
}

/**
 * Extension function for a Pair of PhotonModule and PhotonPipelineResult to update the standard deviations of the estimated robot pose.
 *
 * This function updates the estimated standard deviations of the robot pose using the provided estimated robot pose
 * and the targets from the PhotonPipelineResult.
 *
 * @receiver Pair<PhotonModule, PhotonPipelineResult> The pair of PhotonModule and PhotonPipelineResult.
 * @param estimatedRobotPose Optional<EstimatedRobotPose> The estimated robot pose to use for updating the standard deviations.
 */
fun Pair<PhotonModule, PhotonPipelineResult>.updateStdDev(estimatedRobotPose: Optional<EstimatedRobotPose>) {
    first.updateEstimatedStdDevs(estimatedRobotPose, second.getTargets())
}

/**
 * Extension function for a Pair of PhotonModule and PhotonPipelineResult to update the 3d standard deviations of the estimated robot pose.
 *
 * This function updates the estimated 3d standard deviations of the robot pose using the provided estimated robot pose
 * and the targets from the PhotonPipelineResult.
 *
 * @receiver Pair<PhotonModule, PhotonPipelineResult> The pair of PhotonModule and PhotonPipelineResult.
 * @param estimatedRobotPose Optional<EstimatedRobotPose> The estimated robot pose to use for updating the standard deviations.
 */
fun Pair<PhotonModule, PhotonPipelineResult>.updateStdDev3d(estimatedRobotPose: Optional<EstimatedRobotPose>) {
    first.updateEstimatedStdDevs3d(estimatedRobotPose, second.getTargets())
}

/**
 * Extension function to set the Pingu values of a TalonFXConfiguration using a Pingu object.
 *
 * @receiver TalonFXConfiguration The TalonFX configuration to set the values for.
 * @param pingu Pingu The Pingu object containing the PIDF values.
 */
fun TalonFXConfiguration.setPingu(pingu: Pingu) =
    pingu.apply {
        Slot0.kP = p
        Slot0.kI = i
        Slot0.kD = d
        v ?. run { Slot0.kV = v!! }
        s ?. run { Slot0.kS = s!! }
        g ?. run { Slot0.kG = g!! }
    }

/**
 * Extension function to set the Pingu values of a TalonFXConfiguration using a NetworkPingu object.
 *
 * @receiver TalonFXConfiguration The TalonFX configuration to set the values for.
 * @param pingu NetworkPingu The NetworkPingu object containing the PIDF values.
 */
@SuppressWarnings("kotlin:S6518")
fun TalonFXConfiguration.setPingu(pingu: NetworkPingu) =
    pingu.apply {
        Slot0.kP = p.get()
        Slot0.kI = i.get()
        Slot0.kD = d.get()
        v ?. run { Slot0.kV = v!!.get() }
        s ?. run { Slot0.kS = s!!.get() }
        g ?. run { Slot0.kG = g!!.get() }
    }

/**
 * Extension function to convert a Rotation2d to a Rotation3d.
 *
 * @receiver Rotation2d The 2D rotation to convert.
 * @param yaw Double The yaw value for the 3D rotation.
 * @return Rotation3d The resulting 3D rotation.
 */
fun Rotation2d.to3d(yaw: Double) = Rotation3d(cos, sin, yaw)

/**
 * Operator function to add a yaw value to a Rotation2d, resulting in a Rotation3d.
 *
 * @receiver Rotation2d The 2D rotation to add the yaw to.
 * @param yaw Double The yaw value to add.
 * @return Rotation3d The resulting 3D rotation.
 */
operator fun Rotation2d.plus(yaw: Double) = Rotation3d(cos, sin, yaw)
