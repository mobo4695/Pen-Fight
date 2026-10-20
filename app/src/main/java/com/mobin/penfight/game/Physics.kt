package com.mobin.penfight.game

import kotlin.math.exp
import kotlin.math.hypot

/**
 * The sliding rules of the game, in plain numbers. Pure Kotlin, no Android:
 * the screen feeds in the table size in pixels and gets back the new pen state.
 *
 * Phase 2 rules:
 *  - A sliding pen slows down by friction until it stops.
 *  - The table edge acts like a WALL for now (the pen stops at the edge).
 *    Falling off the table arrives in Phase 3.
 *  - Bumping the other pen also arrives in Phase 3.
 */
object Physics {

    /** How fast sliding dies away. Bigger = pens stop sooner. */
    const val FRICTION_PER_SECOND = 1.35f

    /** How fast spinning dies away. */
    const val SPIN_FRICTION_PER_SECOND = 2.2f

    /** Speed (in table-min-side lengths per second) of a full-power flick. */
    const val MAX_FLICK_SPEED = 2.2f

    /** Below this speed (fraction of the min side, per second) a pen counts as stopped. */
    const val STOP_SPEED = 0.02f

    /** Dragging this far (fraction of the min side) already means full power. */
    const val MAX_DRAG_FRACTION = 0.35f

    /**
     * Moves one pen forward by [dtSeconds].
     *
     * Speeds are handled in pixels-per-second inside here so a flick feels the
     * same in every direction even though the table is taller than it is wide.
     */
    fun stepPen(
        pen: PenState,
        dtSeconds: Float,
        tableWidthPx: Float,
        tableHeightPx: Float,
        penLengthPx: Float,
    ): PenState {
        if (!pen.isMoving() || dtSeconds <= 0f) return pen
        val minSide = minOf(tableWidthPx, tableHeightPx)

        // Current speed in pixels per second.
        val vxPx = pen.vx * tableWidthPx
        val vyPx = pen.vy * tableHeightPx
        val speedPx = hypot(vxPx, vyPx)

        // Friction: exponential slow-down, then a full stop under the threshold.
        var newSpeedPx = speedPx * exp(-FRICTION_PER_SECOND * dtSeconds)
        if (newSpeedPx < STOP_SPEED * minSide) newSpeedPx = 0f

        var newVx = 0f
        var newVy = 0f
        var newX = pen.x
        var newY = pen.y
        if (speedPx > 0f && newSpeedPx > 0f) {
            val dirX = vxPx / speedPx
            val dirY = vyPx / speedPx
            newX += dirX * newSpeedPx * dtSeconds / tableWidthPx
            newY += dirY * newSpeedPx * dtSeconds / tableHeightPx
            newVx = dirX * newSpeedPx / tableWidthPx
            newVy = dirY * newSpeedPx / tableHeightPx
        }

        // Phase 2 wall: the pen's centre may not pass half a pen length from the edge.
        val marginX = (penLengthPx * 0.5f) / tableWidthPx
        val marginY = (penLengthPx * 0.5f) / tableHeightPx
        if (newX < marginX) { newX = marginX; newVx = 0f }
        if (newX > 1f - marginX) { newX = 1f - marginX; newVx = 0f }
        if (newY < marginY) { newY = marginY; newVy = 0f }
        if (newY > 1f - marginY) { newY = 1f - marginY; newVy = 0f }

        // Spin also fades away.
        var newSpin = pen.spin * exp(-SPIN_FRICTION_PER_SECOND * dtSeconds)
        if (kotlin.math.abs(newSpin) < 5f) newSpin = 0f
        val newAngle = pen.angleDegrees + newSpin * dtSeconds

        return pen.copy(
            x = newX, y = newY,
            angleDegrees = newAngle,
            vx = newVx, vy = newVy, spin = newSpin,
        )
    }
}
