package com.mobin.penfight.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobin.penfight.game.PenState
import com.mobin.penfight.game.StartPositions

// Colours of the room around the table and of the table itself.
private val RoomColor = Color(0xFF2E2A26)
private val TableEdgeColor = Color(0xFF6B4A2F)
private val TableTopColor = Color(0xFFC89B6C)
private val TableLineColor = Color(0xFFA97C50)

// The two pens of Phase 1.
private val PlayerPenColor = Color(0xFF2962FF)
private val EnemyPenColor = Color(0xFFD32F2F)

/**
 * Phase 1 screen: the table and the two pens, standing at their start spots.
 * No flicking yet — aiming arrives in Phase 2, bumping and scoring in Phase 3.
 */
@Composable
fun GameScreen() {
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
                .weight(1f),
        ) {
            drawTable()
            drawPen(pen = StartPositions.ENEMY, bodyColor = EnemyPenColor)
            drawPen(pen = StartPositions.PLAYER, bodyColor = PlayerPenColor)
        }

        Text(
            text = "Phase 1: table and pens only. The flick comes next.",
            color = Color(0xFFBCAAA4),
            fontSize = 13.sp,
            modifier = Modifier.padding(top = 12.dp),
        )
    }
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
    val length = minOf(size.width, size.height) * 0.36f
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
