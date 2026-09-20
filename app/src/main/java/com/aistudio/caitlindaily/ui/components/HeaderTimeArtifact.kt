package com.aistudio.caitlindaily.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import com.aistudio.caitlindaily.ui.theme.AmberGold
import com.aistudio.caitlindaily.ui.theme.CoralRed
import com.aistudio.caitlindaily.ui.theme.MintPrimary
import com.aistudio.caitlindaily.ui.theme.OceanBlue
import java.util.Calendar
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

enum class TimeOfDay {
    MORNING,   // 5:00 - 11:59
    AFTERNOON, // 12:00 - 16:59
    EVENING,   // 17:00 - 20:59
    NIGHT      // 21:00 - 4:59
}

@Composable
fun HeaderTimeArtifact(
    modifier: Modifier = Modifier,
    forcedTimeOfDay: TimeOfDay? = null
) {
    val timeOfDay = remember(forcedTimeOfDay) {
        if (forcedTimeOfDay != null) forcedTimeOfDay
        else {
            val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            when (hour) {
                in 5..11 -> TimeOfDay.MORNING
                in 12..16 -> TimeOfDay.AFTERNOON
                in 17..20 -> TimeOfDay.EVENING
                else -> TimeOfDay.NIGHT
            }
        }
    }

    val primaryTint = MaterialTheme.colorScheme.primary
    val tertiaryTint = MaterialTheme.colorScheme.tertiary
    val secondaryTint = MaterialTheme.colorScheme.secondary

    Box(modifier = modifier, contentAlignment = Alignment.TopEnd) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // 1. Organic background contour matching Caitlin Daily aesthetic curvature
            val wavePath = Path().apply {
                moveTo(w * 0.45f, 0f)
                cubicTo(
                    w * 0.55f, h * 0.45f,
                    w * 0.82f, h * 0.70f,
                    w, h * 0.65f
                )
                lineTo(w, 0f)
                close()
            }

            drawPath(
                path = wavePath,
                brush = Brush.radialGradient(
                    colors = listOf(
                        primaryTint.copy(alpha = 0.18f),
                        primaryTint.copy(alpha = 0.04f),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.85f, h * 0.2f),
                    radius = w * 0.65f
                )
            )

            // 2. Dynamic Time of Day Artifact
            when (timeOfDay) {
                TimeOfDay.MORNING -> drawMorningRisingSun(w, h, primaryTint, tertiaryTint)
                TimeOfDay.AFTERNOON -> drawAfternoonHighSun(w, h, primaryTint, tertiaryTint)
                TimeOfDay.EVENING -> drawEveningSunset(w, h, tertiaryTint, CoralRed)
                TimeOfDay.NIGHT -> drawNightCrescentAndStars(w, h, secondaryTint, primaryTint)
            }
        }
    }
}

private fun DrawScope.drawMorningRisingSun(
    w: Float,
    h: Float,
    primaryColor: Color,
    accentColor: Color
) {
    val sunCenterX = w * 0.88f
    val sunCenterY = h * 0.38f
    val sunRadius = 26.dp.toPx()

    // Dawn sunrays
    val rayCount = 7
    for (i in 0 until rayCount) {
        val angle = (170f + i * 25f) * (PI / 180f).toFloat()
        val startR = sunRadius + 6.dp.toPx()
        val endR = sunRadius + 15.dp.toPx()
        drawLine(
            color = accentColor.copy(alpha = 0.22f),
            start = Offset(sunCenterX + cos(angle) * startR, sunCenterY + sin(angle) * startR),
            end = Offset(sunCenterX + cos(angle) * endR, sunCenterY + sin(angle) * endR),
            strokeWidth = 2.dp.toPx()
        )
    }

    // Rising Sun disc
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                accentColor.copy(alpha = 0.32f),
                primaryColor.copy(alpha = 0.15f),
                Color.Transparent
            ),
            center = Offset(sunCenterX, sunCenterY),
            radius = sunRadius * 1.5f
        ),
        radius = sunRadius,
        center = Offset(sunCenterX, sunCenterY)
    )

    // Soft morning horizon arc
    drawArc(
        color = primaryColor.copy(alpha = 0.20f),
        startAngle = 180f,
        sweepAngle = 180f,
        useCenter = false,
        topLeft = Offset(sunCenterX - 36.dp.toPx(), sunCenterY + 12.dp.toPx()),
        size = Size(72.dp.toPx(), 28.dp.toPx()),
        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5.dp.toPx())
    )
}

private fun DrawScope.drawAfternoonHighSun(
    w: Float,
    h: Float,
    primaryColor: Color,
    accentColor: Color
) {
    val sunCenterX = w * 0.88f
    val sunCenterY = h * 0.34f
    val sunRadius = 22.dp.toPx()

    // Outer corona ring
    drawCircle(
        color = accentColor.copy(alpha = 0.12f),
        radius = sunRadius * 1.8f,
        center = Offset(sunCenterX, sunCenterY),
        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx())
    )

    // Mid radiant halo
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                accentColor.copy(alpha = 0.30f),
                primaryColor.copy(alpha = 0.14f),
                Color.Transparent
            ),
            center = Offset(sunCenterX, sunCenterY),
            radius = sunRadius * 1.3f
        ),
        radius = sunRadius,
        center = Offset(sunCenterX, sunCenterY)
    )
}

private fun DrawScope.drawEveningSunset(
    w: Float,
    h: Float,
    amberColor: Color,
    coralColor: Color
) {
    val sunCenterX = w * 0.88f
    val sunCenterY = h * 0.42f
    val sunRadius = 24.dp.toPx()

    // Sinking sun glow
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                coralColor.copy(alpha = 0.32f),
                amberColor.copy(alpha = 0.16f),
                Color.Transparent
            ),
            center = Offset(sunCenterX, sunCenterY),
            radius = sunRadius * 1.4f
        ),
        radius = sunRadius,
        center = Offset(sunCenterX, sunCenterY)
    )

    // Twilight stratified horizon layers
    val horizonY = sunCenterY + 10.dp.toPx()
    drawLine(
        color = amberColor.copy(alpha = 0.25f),
        start = Offset(sunCenterX - 35.dp.toPx(), horizonY),
        end = Offset(sunCenterX + 35.dp.toPx(), horizonY),
        strokeWidth = 2.dp.toPx()
    )
    drawLine(
        color = coralColor.copy(alpha = 0.18f),
        start = Offset(sunCenterX - 22.dp.toPx(), horizonY + 6.dp.toPx()),
        end = Offset(sunCenterX + 22.dp.toPx(), horizonY + 6.dp.toPx()),
        strokeWidth = 1.5.dp.toPx()
    )
}

private fun DrawScope.drawNightCrescentAndStars(
    w: Float,
    h: Float,
    blueColor: Color,
    mintColor: Color
) {
    val moonCenterX = w * 0.88f
    val moonCenterY = h * 0.36f
    val moonR = 18.dp.toPx()

    // Crescent Moon path
    val crescent = Path().apply {
        addOval(androidx.compose.ui.geometry.Rect(moonCenterX - moonR, moonCenterY - moonR, moonCenterX + moonR, moonCenterY + moonR))
    }
    val subtractDisc = Path().apply {
        val offsetDist = moonR * 0.55f
        addOval(androidx.compose.ui.geometry.Rect(moonCenterX - moonR + offsetDist, moonCenterY - moonR - offsetDist * 0.4f, moonCenterX + moonR + offsetDist, moonCenterY + moonR - offsetDist * 0.4f))
    }
    val finalCrescent = Path().apply {
        op(crescent, subtractDisc, androidx.compose.ui.graphics.PathOperation.Difference)
    }

    drawPath(
        path = finalCrescent,
        brush = Brush.linearGradient(
            colors = listOf(
                blueColor.copy(alpha = 0.35f),
                mintColor.copy(alpha = 0.25f)
            ),
            start = Offset(moonCenterX - moonR, moonCenterY - moonR),
            end = Offset(moonCenterX + moonR, moonCenterY + moonR)
        )
    )

    // Twinkling stars (4-point sparkle)
    val starCoords = listOf(
        Pair(moonCenterX - 26.dp.toPx(), moonCenterY - 12.dp.toPx()),
        Pair(moonCenterX - 14.dp.toPx(), moonCenterY + 22.dp.toPx()),
        Pair(moonCenterX + 16.dp.toPx(), moonCenterY + 18.dp.toPx())
    )
    for ((sx, sy) in starCoords) {
        val sSize = 3.5.dp.toPx()
        drawLine(
            color = mintColor.copy(alpha = 0.35f),
            start = Offset(sx - sSize, sy),
            end = Offset(sx + sSize, sy),
            strokeWidth = 1.dp.toPx()
        )
        drawLine(
            color = mintColor.copy(alpha = 0.35f),
            start = Offset(sx, sy - sSize),
            end = Offset(sx, sy + sSize),
            strokeWidth = 1.dp.toPx()
        )
    }
}
