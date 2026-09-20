package com.aistudio.caitlindaily.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.aistudio.caitlindaily.util.TimeOfDayPeriod
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun TimeOfDayArtwork(
    period: TimeOfDayPeriod,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        when (period) {
            TimeOfDayPeriod.MORNING -> drawSunriseArtwork()
            TimeOfDayPeriod.AFTERNOON -> drawAfternoonArtwork()
            TimeOfDayPeriod.EVENING -> drawSunsetArtwork()
            TimeOfDayPeriod.NIGHT -> drawNightArtwork()
        }
    }
}

/**
 * Morning: Rising sun, soft rays, and morning mist/hills.
 */
private fun DrawScope.drawSunriseArtwork() {
    val width = size.width
    val height = size.height

    // Soft morning aura
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFFFFD56B).copy(alpha = 0.28f), Color.Transparent),
            center = Offset(width * 0.82f, height * 0.55f),
            radius = width * 0.5f
        ),
        center = Offset(width * 0.82f, height * 0.55f),
        radius = width * 0.5f
    )

    // Sun disc
    val sunCenter = Offset(width * 0.82f, height * 0.55f)
    drawCircle(
        color = Color(0xFFFFBA49).copy(alpha = 0.35f),
        radius = 28f,
        center = sunCenter
    )

    // Sun rays
    for (i in 0 until 8) {
        val angle = (i * 45f) * (Math.PI / 180f).toFloat()
        val startX = sunCenter.x + cos(angle) * 36f
        val startY = sunCenter.y + sin(angle) * 36f
        val endX = sunCenter.x + cos(angle) * 54f
        val endY = sunCenter.y + sin(angle) * 54f
        drawLine(
            color = Color(0xFFFFD56B).copy(alpha = 0.25f),
            start = Offset(startX, startY),
            end = Offset(endX, endY),
            strokeWidth = 3f
        )
    }

    // Gentle morning hills horizon
    val hillPath = Path().apply {
        moveTo(width * 0.45f, height)
        cubicTo(
            width * 0.6f, height * 0.7f,
            width * 0.75f, height * 0.78f,
            width, height * 0.65f
        )
        lineTo(width, height)
        close()
    }
    drawPath(
        path = hillPath,
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFF4EE394).copy(alpha = 0.15f), Color(0xFF13221C).copy(alpha = 0.05f))
        )
    )
}

/**
 * Afternoon: Bright radiant sun, gentle floating clouds & energy arcs.
 */
private fun DrawScope.drawAfternoonArtwork() {
    val width = size.width
    val height = size.height

    // Warm daylight radiant aura
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFF6CB5FF).copy(alpha = 0.22f), Color.Transparent),
            center = Offset(width * 0.85f, height * 0.45f),
            radius = width * 0.5f
        ),
        center = Offset(width * 0.85f, height * 0.45f),
        radius = width * 0.5f
    )

    // Sun core
    val sunCenter = Offset(width * 0.85f, height * 0.45f)
    drawCircle(
        color = Color(0xFFFFC837).copy(alpha = 0.32f),
        radius = 32f,
        center = sunCenter
    )

    // Outer energy corona
    drawCircle(
        color = Color(0xFFFFE082).copy(alpha = 0.18f),
        radius = 48f,
        center = sunCenter,
        style = Stroke(width = 3f)
    )

    // Minimalist floating cloud
    val cloudPath = Path().apply {
        moveTo(width * 0.65f, height * 0.65f)
        cubicTo(
            width * 0.68f, height * 0.52f,
            width * 0.78f, height * 0.52f,
            width * 0.82f, height * 0.62f
        )
        cubicTo(
            width * 0.88f, height * 0.62f,
            width * 0.94f, height * 0.7f,
            width * 0.92f, height * 0.78f
        )
        lineTo(width * 0.65f, height * 0.78f)
        close()
    }
    drawPath(
        path = cloudPath,
        brush = Brush.linearGradient(
            colors = listOf(Color(0xFFE2F0FF).copy(alpha = 0.22f), Color(0xFF6CB5FF).copy(alpha = 0.1f))
        )
    )
}

/**
 * Evening: Golden hour sunset dipping below twilight horizon.
 */
private fun DrawScope.drawSunsetArtwork() {
    val width = size.width
    val height = size.height

    // Sunset twilight gradient glow
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFFFF8A65).copy(alpha = 0.25f),
                Color(0xFFBA68C8).copy(alpha = 0.12f),
                Color.Transparent
            ),
            center = Offset(width * 0.82f, height * 0.62f),
            radius = width * 0.52f
        ),
        center = Offset(width * 0.82f, height * 0.62f),
        radius = width * 0.52f
    )

    // Dipping sunset disc
    drawCircle(
        color = Color(0xFFFF7043).copy(alpha = 0.32f),
        radius = 30f,
        center = Offset(width * 0.82f, height * 0.60f)
    )

    // Dusk horizon horizontal reflection lines
    for (i in 0..3) {
        val y = height * (0.65f + i * 0.08f)
        val alpha = 0.22f - (i * 0.04f)
        drawLine(
            brush = Brush.horizontalGradient(
                colors = listOf(Color.Transparent, Color(0xFFFFB74D).copy(alpha = alpha), Color.Transparent)
            ),
            start = Offset(width * 0.55f, y),
            end = Offset(width, y),
            strokeWidth = 4f
        )
    }
}

/**
 * Night: Sleek crescent moon, twinkling stars, and soft sleeping cloud with restful zZz waves.
 */
private fun DrawScope.drawNightArtwork() {
    val width = size.width
    val height = size.height

    // Soft deep night ambient glow
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFF81D4FA).copy(alpha = 0.18f), Color.Transparent),
            center = Offset(width * 0.82f, height * 0.42f),
            radius = width * 0.45f
        ),
        center = Offset(width * 0.82f, height * 0.42f),
        radius = width * 0.45f
    )

    // Crescent moon using overlapping arcs/paths
    val moonCenter = Offset(width * 0.84f, height * 0.42f)
    val moonPath = Path().apply {
        moveTo(moonCenter.x + 8f, moonCenter.y - 28f)
        cubicTo(
            moonCenter.x - 30f, moonCenter.y - 12f,
            moonCenter.x - 30f, moonCenter.y + 20f,
            moonCenter.x + 12f, moonCenter.y + 28f
        )
        cubicTo(
            moonCenter.x - 14f, moonCenter.y + 14f,
            moonCenter.x - 14f, moonCenter.y - 14f,
            moonCenter.x + 8f, moonCenter.y - 28f
        )
        close()
    }
    drawPath(
        path = moonPath,
        color = Color(0xFFFFF9C4).copy(alpha = 0.35f)
    )

    // Twinkling 4-point stars
    drawStar(Offset(width * 0.68f, height * 0.28f), 7f, Color(0xFFE1F5FE).copy(alpha = 0.35f))
    drawStar(Offset(width * 0.94f, height * 0.32f), 5f, Color(0xFFE1F5FE).copy(alpha = 0.30f))
    drawStar(Offset(width * 0.74f, height * 0.65f), 6f, Color(0xFFFFF9C4).copy(alpha = 0.28f))

    // Delicate sleeping cloud & rest wave
    val sleepingCloud = Path().apply {
        moveTo(width * 0.70f, height * 0.75f)
        cubicTo(
            width * 0.75f, height * 0.65f,
            width * 0.85f, height * 0.65f,
            width * 0.90f, height * 0.72f
        )
        cubicTo(
            width * 0.96f, height * 0.74f,
            width * 0.98f, height * 0.82f,
            width * 0.92f, height * 0.86f
        )
        lineTo(width * 0.68f, height * 0.86f)
        close()
    }
    drawPath(
        path = sleepingCloud,
        brush = Brush.linearGradient(
            colors = listOf(Color(0xFF90CAF9).copy(alpha = 0.16f), Color(0xFF37474F).copy(alpha = 0.08f))
        )
    )

    // Tiny sleeping zZz indication curves above the cloud
    val zStroke = Stroke(width = 2.5f)
    val zColor = Color(0xFFB0BEC5).copy(alpha = 0.25f)
    // small z
    drawLine(zColor, Offset(width * 0.88f, height * 0.60f), Offset(width * 0.91f, height * 0.60f), strokeWidth = 2f)
    drawLine(zColor, Offset(width * 0.91f, height * 0.60f), Offset(width * 0.88f, height * 0.64f), strokeWidth = 2f)
    drawLine(zColor, Offset(width * 0.88f, height * 0.64f), Offset(width * 0.91f, height * 0.64f), strokeWidth = 2f)

    // slightly larger Z
    drawLine(zColor, Offset(width * 0.92f, height * 0.52f), Offset(width * 0.96f, height * 0.52f), strokeWidth = 2.5f)
    drawLine(zColor, Offset(width * 0.96f, height * 0.52f), Offset(width * 0.92f, height * 0.57f), strokeWidth = 2.5f)
    drawLine(zColor, Offset(width * 0.92f, height * 0.57f), Offset(width * 0.96f, height * 0.57f), strokeWidth = 2.5f)
}

private fun DrawScope.drawStar(center: Offset, size: Float, color: Color) {
    drawLine(color, Offset(center.x - size, center.y), Offset(center.x + size, center.y), strokeWidth = 2f)
    drawLine(color, Offset(center.x, center.y - size), Offset(center.x, center.y + size), strokeWidth = 2f)
}
