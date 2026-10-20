package com.mobin.penfight.game

/**
 * Where one pen is on the table.
 *
 * Positions are RELATIVE: x and y go from 0f (left / top edge of the play field)
 * to 1f (right / bottom edge). The screen converts them to pixels when drawing,
 * so the game logic never cares about the phone's screen size.
 *
 * Later phases add speed and spin to this state; Phase 1 only places the pens.
 */
data class PenState(
    val x: Float,
    val y: Float,
    /** Which way the pen's tip points, in degrees. 0 = right, -90 = up, 90 = down. */
    val angleDegrees: Float,
)

/** The two starting spots: enemy at the top pointing down, player at the bottom pointing up. */
object StartPositions {
    val ENEMY = PenState(x = 0.5f, y = 0.20f, angleDegrees = 90f)
    val PLAYER = PenState(x = 0.5f, y = 0.80f, angleDegrees = -90f)
}
