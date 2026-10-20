package com.mobin.penfight.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobin.penfight.game.PenState
import com.mobin.penfight.game.Physics
import com.mobin.penfight.game.StartPositions
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

// Colours of the room around the table and of the table itself.
private val RoomColor = Color(0xFF2E2A26)
private val TableEdgeColor = Color(0xFF6B4A2F)
private val TableTopColor = Color(0xFFC89B6C)
private val TableLineColor = Color(0xFFA97C50)
private val AimColor = Color(0xFFFFD54F)

// The two pens.
private val PlayerPenColor = Color(0xFF2962FF)
private val EnemyPenColor = Color(0xFFD32F2F)

/** How long a drawn pen is, in pixels: about a third of the table's smaller side. */
private fun penLengthPx(widthPx: Float, heightPx: Float): Float =
    minOf(widthPx, heightPx) * 0.36f

/**
 * Phase 2 screen: the table, the two pens, and THE FLICK.
 *
 * Drag back on the blue pen like a slingshot: a golden arrow shows the way the
 * pen will fly and how strong the shot is. Let go and the pen slides, slowing
 * down by friction until it stops. The table edge still works like a wall —
 * falling off, bumping and points arrive in Phase 3.
 */
@Composable
fun GameScreen() {
    var playerPen by remember { mutableStateOf(StartPositions.PLAYER) }
    var enemyPen by remember { mutableStateOf(StartPositions.ENEMY) }
    var tableSize by remember { mutableStateOf(IntSize.Zero) }
    // Where the finger is right now while aiming (pixels), or null when not aiming.
    var aimPoint by remember { mutableStateOf<Offset?>(null) }

    val pensStopped = !playerPen.isMoving() && !enemyPen.isMoving()

    // The heartbeat of the game: every screen frame, move any sliding pen a little.
    var lastFrameMillis by remember { mutableLongStateOf(0L) }
    LaunchedEffect(Unit) {
        while (true) {
            withFrameMillis { now ->
                if (lastFrameMillis != 0L && tableSize.width > 0) {
                    val dt = ((now - lastFrameMillis) / 1000f).coerceIn(0f, 0.05f)
                    if (dt > 0f) {
                        val w = tableSize.width.toFloat()
                        val h = tableSize.height.toFloat()
                        val len = penLengthPx(w, h)
                        if (playerPen.isMoving()) {
                            playerPen = Physics.stepPen(playerPen, dt, w, h, len)
                        }
                        if (enemyPen.isMoving()) {
                            enemyPen = Physics.stepPen(enemyPen, dt, w, h, len)
                        }
                    }
                }
                lastFrameMillis = now
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(RoomColor)
            .padding(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "PEN FIGHT",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "You 0 — 0 Enemy",
                color = Color(0xFFFFD54F),
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Text(
            text = "First to 5 points wins the match",
            color = Color(0xFFBCAAA4),
            fontSize = 13.sp,
            modifier = Modifier.padding(top = 2.dp, bottom = 12.dp),
        )

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .onSizeChanged { tableSize = it }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { touch ->
                            // Aiming only counts when the drag starts near your pen
                            // (a generous grab area, like the whole pen plus a margin).
                            // Read the pen states directly here so the check is always live.
                            val stoppedNow = !playerPen.isMoving() && !enemyPen.isMoving()
                            if (stoppedNow && tableSize.width > 0) {
                                val w = tableSize.width.toFloat()
                                val h = tableSize.height.toFloat()
                                val centre = Offset(playerPen.x * w, playerPen.y * h)
                                if ((touch - centre).getDistance() <= penLengthPx(w, h) * 0.8f) {
                                    aimPoint = touch
                                }
                            }
                        },
                        onDrag = { change, _ ->
                            if (aimPoint != null) aimPoint = change.position
                        },
                        onDragEnd = {
                            val drag = aimPoint
                            aimPoint = null
                            if (drag != null && tableSize.width > 0) {
                                val w = tableSize.width.toFloat()
                                val h = tableSize.height.toFloat()
                                val centre = Offset(playerPen.x * w, playerPen.y * h)
                                // Slingshot: the pen flies AWAY from the pulled point.
                                val pull = centre - drag
                                val dist = pull.getDistance()
                                val minSide = minOf(w, h)
                                val power = (dist / (Physics.MAX_DRAG_FRACTION * minSide))
                                    .coerceIn(0f, 1f)
                                if (power >= 0.06f && dist > 0f) {
                                    val speedPx = power * Physics.MAX_FLICK_SPEED * minSide
                                    playerPen = playerPen.copy(
                                        vx = pull.x / dist * speedPx / w,
                                        vy = pull.y / dist * speedPx / h,
                                    )
                                }
                            }
                        },
                        onDragCancel = { aimPoint = null },
                    )
                },
        ) {
            drawTable()
            drawPen(pen = enemyPen, bodyColor = EnemyPenColor)
            drawPen(pen = playerPen, bodyColor = PlayerPenColor)
            val drag = aimPoint
            if (drag != null) {
                drawAimArrow(pen = playerPen, dragPoint = drag)
            }
        }

        Text(
            text = if (pensStopped) {
                "Your move: drag back on your blue pen and let go."
            } else {
                "Sliding…"
            },
            color = Color(0xFFBCAAA4),
            fontSize = 13.sp,
            modifier = Modifier.padding(top = 12.dp),
        )
    }
}

/** The golden slingshot arrow: from the pen, pointing where it will fly. */
private fun DrawScope.drawAimArrow(pen: PenState, dragPoint: Offset) {
    val centre = Offset(pen.x * size.width, pen.y * size.height)
    val pull = centre - dragPoint
    val dist = pull.getDistance()
    if (dist < 4f) return
    val minSide = minOf(size.width, size.height)
    val power = (dist / (Physics.MAX_DRAG_FRACTION * minSide)).coerceIn(0f, 1f)
    val dir = Offset(pull.x / dist, pull.y / dist)

    // The rubber band between the pen and the finger.
    drawLine(
        color = Color.White.copy(alpha = 0.45f),
        start = centre,
        end = dragPoint,
        strokeWidth = 3.dp.toPx(),
        cap = StrokeCap.Round,
    )
    // The arrow shaft, longer with more power.
    val arrowLength = 26.dp.toPx() + power * minSide * 0.34f
    val arrowEnd = centre + dir * arrowLength
    drawLine(
        color = AimColor,
        start = centre,
        end = arrowEnd,
        strokeWidth = 6.dp.toPx(),
        cap = StrokeCap.Round,
    )
    // The arrow head: two short wings at the end.
    val angle = atan2(dir.y, dir.x)
    val wing = 16.dp.toPx()
    for (side in listOf(150f, -150f)) {
        val wingAngle = angle + Math.toRadians(side.toDouble()).toFloat()
        val wingEnd = arrowEnd + Offset(cos(wingAngle) * wing, sin(wingAngle) * wing)
        drawLine(
            color = AimColor,
            start = arrowEnd,
            end = wingEnd,
            strokeWidth = 6.dp.toPx(),
            cap = StrokeCap.Round,
        )
    }
    // A ring around the pen that fills with power (alpha grows as you pull).
    drawCircle(
        color = AimColor.copy(alpha = 0.25f + power * 0.55f),
        radius = penLengthPx(size.width, size.height) * 0.55f,
        center = centre,
        style = Stroke(width = 3.dp.toPx()),
    )
}

/** Draws the wooden table: a dark edge rim and a lighter top with a thin inner line. */
private fun DrawScope.drawTable() {
    val rim = 14.dp.toPx()
    val corner = 24.dp.toPx()
    // Edge rim (the side of the table).
    drawRoundRect(
        color = TableEdgeColor,
        topLeft = Offset.Zero,
        size = size,
        cornerRadius = CornerRadius(corner, corner),
    )
    // Table top.
    drawRoundRect(
        color = TableTopColor,
        topLeft = Offset(rim, rim),
        size = Size(size.width - rim * 2, size.height - rim * 2),
        cornerRadius = CornerRadius(corner - 6.dp.toPx(), corner - 6.dp.toPx()),
    )
    // Thin line inside the top, like a polished border.
    val lineInset = rim + 12.dp.toPx()
    drawRoundRect(
        color = TableLineColor,
        topLeft = Offset(lineInset, lineInset),
        size = Size(size.width - lineInset * 2, size.height - lineInset * 2),
        cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx()),
        style = Stroke(width = 2.dp.toPx()),
    )
}

/**
 * Draws one pen. Inside the rotated frame the pen lies along the +X axis:
 * back of the pen at -length/2, writing tip at +length/2.
 */
private fun DrawScope.drawPen(pen: PenState, bodyColor: Color) {
    val centerX = pen.x * size.width
    val centerY = pen.y * size.height
    val length = penLengthPx(size.width, size.height)
    val width = length / 5.5f

    translate(left = centerX, top = centerY) {
        rotate(degrees = pen.angleDegrees, pivot = Offset.Zero) {
            val half = length / 2f
            val halfW = width / 2f

            // Body: the back 76% of the pen, a capsule shape.
            drawRoundRect(
                color = bodyColor,
                topLeft = Offset(-half, -halfW),
                size = Size(length * 0.76f, width),
                cornerRadius = CornerRadius(halfW, halfW),
            )
            // Cap band near the back end (darker strip).
            drawRect(
                color = bodyColor.copy(alpha = 0.55f),
                topLeft = Offset(-half + length * 0.05f, -halfW),
                size = Size(length * 0.05f, width),
            )
            // Pocket clip on top of the body.
            drawRoundRect(
                color = bodyColor.copy(alpha = 0.7f),
                topLeft = Offset(-half + length * 0.14f, -halfW),
                size = Size(length * 0.24f, width * 0.20f),
                cornerRadius = CornerRadius(width * 0.1f, width * 0.1f),
            )
            // Cone tip: pale triangle from the body to the writing point.
            val cone = Path().apply {
                moveTo(half - length * 0.24f, -halfW)
                lineTo(half, 0f)
                lineTo(half - length * 0.24f, halfW)
                close()
            }
            drawPath(path = cone, color = Color(0xFFFFE9C9))
            // The writing point itself.
            drawCircle(
                color = Color(0xFF263238),
                radius = width * 0.09f,
                center = Offset(half, 0f),
            )
        }
    }
}
