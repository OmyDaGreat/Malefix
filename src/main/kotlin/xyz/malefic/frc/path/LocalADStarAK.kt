package xyz.malefic.frc.path

import com.pathplanner.lib.path.GoalEndState
import com.pathplanner.lib.path.PathConstraints
import com.pathplanner.lib.path.PathPlannerPath
import com.pathplanner.lib.path.PathPoint
import com.pathplanner.lib.pathfinding.LocalADStar
import com.pathplanner.lib.pathfinding.Pathfinder
import edu.wpi.first.math.Pair
import edu.wpi.first.math.geometry.Translation2d
import org.littletonrobotics.junction.LogTable
import org.littletonrobotics.junction.Logger
import org.littletonrobotics.junction.inputs.LoggableInputs

/**
 * A class that implements the Pathfinder interface using the LocalADStar algorithm. This class is
 * responsible for calculating paths for a robot to follow on a field, taking into account dynamic
 * obstacles and other constraints.
 *
 *
 * Usage in FRC: This class is used in FRC (FIRST Robotics Competition) to navigate the robot
 * autonomously on the field. It calculates the optimal path from a start position to a goal
 * position while avoiding obstacles. The path is updated dynamically based on the current state of
 * the field and the robot's position.
 */
class LocalADStarAK : Pathfinder {
    private val io = ADStarIO()

    /**
     * Get if a new path has been calculated since the last time a path was retrieved. This method
     * checks if a new path is available and logs the current state.
     *
     * @return True if a new path is available.
     */
    override fun isNewPathAvailable(): Boolean {
        if (!Logger.hasReplaySource()) {
            io.updateIsNewPathAvailable()
        }

        Logger.processInputs("LocalADStarAK", io)

        return io.isNewPathAvailable
    }

    /**
     * Get the most recently calculated path. This method retrieves the current path based on the
     * provided constraints and goal end state.
     *
     * @param constraints The path constraints to use when creating the path.
     * @param goalEndState The goal end state to use when creating the path.
     * @return The PathPlannerPath created from the points calculated by the pathfinder.
     */
    override fun getCurrentPath(
        constraints: PathConstraints,
        goalEndState: GoalEndState,
    ): PathPlannerPath? {
        if (!Logger.hasReplaySource()) {
            io.updateCurrentPathPoints(constraints, goalEndState)
        }

        Logger.processInputs("LocalADStarAK", io)

        if (io.currentPathPoints.isEmpty()) {
            return null
        }

        return PathPlannerPath.fromPathPoints(io.currentPathPoints, constraints, goalEndState)
    }

    /**
     * Set the start position to pathfind from. This method sets the initial position for the
     * pathfinding algorithm.
     *
     * @param startPosition Start position on the field. If this is within an obstacle it will be
     * moved to the nearest non-obstacle node.
     */
    override fun setStartPosition(startPosition: Translation2d?) {
        if (!Logger.hasReplaySource()) {
            io.adStar.setStartPosition(startPosition)
        }
    }

    /**
     * Set the goal position to pathfind to. This method sets the target position for the pathfinding
     * algorithm.
     *
     * @param goalPosition Goal position on the field. If this is within an obstacle it will be moved
     * to the nearest non-obstacle node.
     */
    override fun setGoalPosition(goalPosition: Translation2d?) {
        if (!Logger.hasReplaySource()) {
            io.adStar.setGoalPosition(goalPosition)
        }
    }

    /**
     * Set the dynamic obstacles that should be avoided while pathfinding. This method updates the
     * list of obstacles that the pathfinding algorithm should avoid.
     *
     * @param obs A List of Translation2d pairs representing obstacles. Each Translation2d represents
     * opposite corners of a bounding box.
     * @param currentRobotPos The current position of the robot. This is needed to change the start
     * position of the path to properly avoid obstacles.
     */
    override fun setDynamicObstacles(
        obs: MutableList<Pair<Translation2d?, Translation2d?>?>?,
        currentRobotPos: Translation2d?,
    ) {
        if (!Logger.hasReplaySource()) {
            io.adStar.setDynamicObstacles(obs, currentRobotPos)
        }
    }

    /**
     * A class that handles the input/output operations for the LocalADStar pathfinding algorithm.
     * Implements the LoggableInputs interface to allow logging of pathfinding data. This class is
     * responsible for managing the state of the pathfinding algorithm, including whether a new path
     * is available and the current path points.
     */
    private class ADStarIO : LoggableInputs {
        var adStar: LocalADStar = LocalADStar()
        var isNewPathAvailable: Boolean = false
        var currentPathPoints: MutableList<PathPoint> = mutableListOf<PathPoint>()

        /**
         * Logs the current state of the pathfinding algorithm to the provided LogTable.
         *
         * @param table The LogTable to log the data to.
         */
        override fun toLog(table: LogTable) {
            table.put("IsNewPathAvailable", isNewPathAvailable)

            val pointsLogged = DoubleArray(currentPathPoints.size * 2)
            var idx = 0
            for (point in currentPathPoints) {
                pointsLogged[idx] = point.position.x
                pointsLogged[idx + 1] = point.position.y
                idx += 2
            }

            table.put("CurrentPathPoints", pointsLogged)
        }

        /**
         * Restores the state of the pathfinding algorithm from the provided LogTable.
         *
         * @param table The LogTable to restore the data from.
         */
        override fun fromLog(table: LogTable) {
            isNewPathAvailable = table["IsNewPathAvailable"].getBoolean(false)

            val pointsLogged = table["CurrentPathPoints"].getDoubleArray(DoubleArray(0))

            val pathPoints: MutableList<PathPoint> = ArrayList<PathPoint>(pointsLogged.size / 2)
            var i = 0
            while (i < pointsLogged.size) {
                pathPoints.add(
                    PathPoint(Translation2d(pointsLogged[i], pointsLogged[i + 1]), null),
                )
                i += 2
            }

            currentPathPoints = pathPoints
        }

        /**
         * Updates the isNewPathAvailable flag by querying the LocalADStar instance. This method checks
         * if a new path has been calculated by the pathfinding algorithm.
         */
        fun updateIsNewPathAvailable() {
            isNewPathAvailable = adStar.isNewPathAvailable
        }

        /**
         * Updates the current path points by querying the LocalADStar instance with the provided
         * constraints and goal end state. This method retrieves the latest path points calculated by
         * the pathfinding algorithm.
         *
         * @param constraints The path constraints to use when creating the path.
         * @param goalEndState The goal end state to use when creating the path.
         */
        fun updateCurrentPathPoints(
            constraints: PathConstraints,
            goalEndState: GoalEndState,
        ) {
            val currentPath = adStar.getCurrentPath(constraints, goalEndState)

            if (currentPath != null) {
                currentPathPoints = currentPath.allPathPoints
            } else {
                currentPathPoints = mutableListOf<PathPoint>()
            }
        }
    }
}
