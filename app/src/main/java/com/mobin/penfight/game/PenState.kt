package com.mobin.penfight.game

/**
 * Full state of one pen on the table.
 *
 * Positions are RELATIVE: x and y go from 0f (left / top edge of the play field)
 * to 1f (right / bottom edge). Speeds (vx, vy) are in the same relative units
 * per second. The screen converts to pixels when drawing, so the game logic
 * never cares about the phone's screen size.
 *
 * Spin is the turning speed in degrees per second (positive = clockwise).
 */
data class PenState(
    val x: Float,
    val y: Float,
    /** Which way the pen's tip points, in degrees. 0 = right, -90 = up, 90 = down. */
    val angleDegrees: Float,
    val vx: Float = 0f,
    val vy: Float = 0f,
    val spin: Float = 0f,
) {
    /** True while this pen is still sliding or spinning. */
    fun isMoving(): Boolean = vx != 0f || vy != 0f || spin != 0f
}

/** The two starting spots: enemy at the top pointing down, player at the bottom pointing up. */
object StartPositions {
    val ENEMY = PenState(x = 0.5f, y = 0.20f, angleDegrees = 90f)
    val PLAYER = PenState(x = 0.5f, y = 0.80f, angleDegrees = -90f)
}
