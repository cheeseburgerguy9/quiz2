package com.aswinas.caitlin.planner.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.unit.dp
import com.aswinas.caitlin.planner.ui.theme.AmberGold
import com.aswinas.caitlin.planner.ui.theme.AuroraCyan
import com.aswinas.caitlin.planner.ui.theme.CoralRed
import com.aswinas.caitlin.planner.ui.theme.ElectricViolet
import com.aswinas.caitlin.planner.ui.theme.MintPrimary
import com.aswinas.caitlin.planner.ui.theme.OceanBlue
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun WizardAnimatedIllustration(
    stepIndex: Int,
    modifier: Modifier = Modifier.size(180.dp)
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        when (stepIndex) {
            0 -> AgendaFlowIllustration()
            1 -> GeminiVisionIllustration()
            2 -> CalendarSyncIllustration()
            3 -> VelocityAnalyticsIllustration()
            else -> AgendaFlowIllustration()
        }
    }
}

/**
 * Step 1: Animated Daily Agenda & Time Stream
 */
@Composable
fun AgendaFlowIllustration(
    modifier: Modifier = Modifier.fillMaxSize()
) {
    val transition = rememberInfiniteTransition(label = "agenda_flow")

    val orbitAngle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbit_rot"
    )

    val sweepAngle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "clock_sweep"
    )

    val bounceOffset by transition.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "card_bounce"
    )

    val pulseGlow by transition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_pulse"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val center = Offset(w / 2f, h / 2f + bounceOffset)

        // Outer ambient glow ring
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(MintPrimary.copy(alpha = 0.25f * pulseGlow), Color.Transparent),
                center = center,
                radius = w * 0.48f
            ),
            radius = w * 0.48f,
            center = center
        )

        // Orbiting dashed arc
        rotate(orbitAngle, center) {
            drawCircle(
                color = MintPrimary.copy(alpha = 0.35f),
                radius = w * 0.42f,
                center = center,
                style = Stroke(
                    width = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
            )

            // Orbiting satellite dots
            val sat1 = Offset(center.x + w * 0.42f * cos(0.0).toFloat(), center.y + w * 0.42f * sin(0.0).toFloat())
            val sat2 = Offset(center.x + w * 0.42f * cos(PI.toFloat()), center.y + w * 0.42f * sin(PI.toFloat()))
            drawCircle(color = AuroraCyan, radius = 5.dp.toPx(), center = sat1)
            drawCircle(color = MintPrimary, radius = 4.dp.toPx(), center = sat2)
        }

        // Central Squircle Agenda Card
        val cardW = w * 0.62f
        val cardH = h * 0.62f
        val cardTopLeft = Offset(center.x - cardW / 2f, center.y - cardH / 2f)

        drawRoundRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFF163827),
                    Color(0xFF0F261B)
                ),
                start = cardTopLeft,
                end = Offset(cardTopLeft.x + cardW, cardTopLeft.y + cardH)
            ),
            topLeft = cardTopLeft,
            size = Size(cardW, cardH),
            cornerRadius = CornerRadius(24.dp.toPx(), 24.dp.toPx())
        )

        drawRoundRect(
            color = MintPrimary.copy(alpha = 0.6f),
            topLeft = cardTopLeft,
            size = Size(cardW, cardH),
            cornerRadius = CornerRadius(24.dp.toPx(), 24.dp.toPx()),
            style = Stroke(width = 2.dp.toPx())
        )

        // Clock Face in top right
        val clockCenter = Offset(cardTopLeft.x + cardW * 0.72f, cardTopLeft.y + cardH * 0.30f)
        val clockR = cardW * 0.18f
        drawCircle(
            color = Color(0xFF0A1F15),
            radius = clockR,
            center = clockCenter
        )
        drawCircle(
            color = MintPrimary.copy(alpha = 0.5f),
            radius = clockR,
            center = clockCenter,
            style = Stroke(width = 1.5.dp.toPx())
        )

        // Sweeping minute hand
        val sweepRad = Math.toRadians(sweepAngle.toDouble())
        val handEnd = Offset(
            clockCenter.x + (clockR * 0.75f) * cos(sweepRad).toFloat(),
            clockCenter.y + (clockR * 0.75f) * sin(sweepRad).toFloat()
        )
        drawLine(
            color = AuroraCyan,
            start = clockCenter,
            end = handEnd,
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )
        // Fixed hour hand
        val hourEnd = Offset(clockCenter.x, clockCenter.y - clockR * 0.50f)
        drawLine(
            color = MintPrimary,
            start = clockCenter,
            end = hourEnd,
            strokeWidth = 2.5.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Task Items Pills inside card
        val pillX = cardTopLeft.x + cardW * 0.14f
        val pillW = cardW * 0.45f
        val pillH = 10.dp.toPx()

        // Pill 1 (Completed with check)
        val pill1Y = cardTopLeft.y + cardH * 0.22f
        drawRoundRect(
            color = MintPrimary.copy(alpha = 0.85f),
            topLeft = Offset(pillX, pill1Y),
            size = Size(pillW * 0.9f, pillH),
            cornerRadius = CornerRadius(50f, 50f)
        )
        drawCircle(
            color = Color.White,
            radius = 3.dp.toPx(),
            center = Offset(pillX + pillH / 2f + 2f, pill1Y + pillH / 2f)
        )

        // Pill 2 (In progress)
        val pill2Y = cardTopLeft.y + cardH * 0.44f
        drawRoundRect(
            color = AuroraCyan.copy(alpha = 0.75f),
            topLeft = Offset(pillX, pill2Y),
            size = Size(cardW * 0.68f, pillH),
            cornerRadius = CornerRadius(50f, 50f)
        )

        // Pill 3
        val pill3Y = cardTopLeft.y + cardH * 0.64f
        drawRoundRect(
            color = Color(0xFF335C48),
            topLeft = Offset(pillX, pill3Y),
            size = Size(cardW * 0.50f, pillH),
            cornerRadius = CornerRadius(50f, 50f)
        )

        // Little green check pill at bottom
        val checkCenter = Offset(cardTopLeft.x + cardW * 0.80f, cardTopLeft.y + cardH * 0.78f)
        drawCircle(color = MintPrimary, radius = 9.dp.toPx(), center = checkCenter)
        val checkPath = Path().apply {
            moveTo(checkCenter.x - 4.dp.toPx(), checkCenter.y)
            lineTo(checkCenter.x - 1.dp.toPx(), checkCenter.y + 3.dp.toPx())
            lineTo(checkCenter.x + 4.dp.toPx(), checkCenter.y - 3.dp.toPx())
        }
        drawPath(checkPath, color = Color(0xFF072416), style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))
    }
}

/**
 * Step 2: Animated Gemini 3.6 Flash Multi-Modal Vision & AI Verification
 */
@Composable
fun GeminiVisionIllustration(
    modifier: Modifier = Modifier.fillMaxSize()
) {
    val transition = rememberInfiniteTransition(label = "gemini_vision")

    val starRotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(16000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "star_rot"
    )

    val starScale by transition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.14f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "star_scale"
    )

    val scanProgress by transition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_scan"
    )

    val nodePulse by transition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "node_pulse"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val center = Offset(w / 2f, h / 2f)

        // Radial Aurora Glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    ElectricViolet.copy(alpha = 0.35f * starScale),
                    AuroraCyan.copy(alpha = 0.15f),
                    Color.Transparent
                ),
                center = center,
                radius = w * 0.48f
            ),
            radius = w * 0.48f,
            center = center
        )

        // Document / Screenshot Card being scanned
        val docW = w * 0.65f
        val docH = h * 0.68f
        val docTopLeft = Offset(center.x - docW / 2f, center.y - docH / 2f + 4.dp.toPx())

        drawRoundRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFF20133A),
                    Color(0xFF140D26)
                )
            ),
            topLeft = docTopLeft,
            size = Size(docW, docH),
            cornerRadius = CornerRadius(22.dp.toPx(), 22.dp.toPx())
        )

        drawRoundRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    ElectricViolet.copy(alpha = 0.8f),
                    AuroraCyan.copy(alpha = 0.4f)
                )
            ),
            topLeft = docTopLeft,
            size = Size(docW, docH),
            cornerRadius = CornerRadius(22.dp.toPx(), 22.dp.toPx()),
            style = Stroke(width = 2.dp.toPx())
        )

        // Holographic document grid & placeholder lines inside
        val lineX = docTopLeft.x + docW * 0.15f
        val lineW = docW * 0.70f
        for (i in 0..2) {
            val lineY = docTopLeft.y + docH * (0.24f + i * 0.14f)
            drawRoundRect(
                color = ElectricViolet.copy(alpha = 0.25f),
                topLeft = Offset(lineX, lineY),
                size = Size(if (i == 1) lineW * 0.6f else lineW * 0.85f, 6.dp.toPx()),
                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
            )
        }

        // Animated Scanning Beam
        val scanY = docTopLeft.y + docH * scanProgress
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    AuroraCyan.copy(alpha = 0.0f),
                    AuroraCyan.copy(alpha = 0.25f),
                    AuroraCyan.copy(alpha = 0.85f),
                    AuroraCyan.copy(alpha = 0.25f),
                    AuroraCyan.copy(alpha = 0.0f)
                ),
                startY = scanY - 16.dp.toPx(),
                endY = scanY + 16.dp.toPx()
            ),
            topLeft = Offset(docTopLeft.x + 4.dp.toPx(), scanY - 16.dp.toPx()),
            size = Size(docW - 8.dp.toPx(), 32.dp.toPx())
        )
        drawLine(
            color = AuroraCyan,
            start = Offset(docTopLeft.x + 8.dp.toPx(), scanY),
            end = Offset(docTopLeft.x + docW - 8.dp.toPx(), scanY),
            strokeWidth = 2.5.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Central 4-pointed Gemini AI Sparkle Star
        val starCenter = Offset(center.x + docW * 0.24f, center.y - docH * 0.22f)
        rotate(starRotation, starCenter) {
            scale(starScale, starScale, starCenter) {
                drawGeminiSparkle(starCenter, 22.dp.toPx(), ElectricViolet, AuroraCyan)
            }
        }

        // Secondary mini sparkle
        val miniCenter = Offset(docTopLeft.x + docW * 0.22f, docTopLeft.y + docH * 0.74f)
        scale(1.2f - (starScale - 1f), 1.2f - (starScale - 1f), miniCenter) {
            drawGeminiSparkle(miniCenter, 12.dp.toPx(), AmberGold, MintPrimary)
        }

        // Verified Shield Badge at bottom right
        val shieldCenter = Offset(docTopLeft.x + docW * 0.82f, docTopLeft.y + docH * 0.82f)
        drawCircle(color = MintPrimary, radius = 13.dp.toPx(), center = shieldCenter)
        drawCircle(color = Color(0xFF082B1B), radius = 13.dp.toPx(), center = shieldCenter, style = Stroke(width = 2.dp.toPx()))

        val vPath = Path().apply {
            moveTo(shieldCenter.x - 5.dp.toPx(), shieldCenter.y)
            lineTo(shieldCenter.x - 1.dp.toPx(), shieldCenter.y + 4.dp.toPx())
            lineTo(shieldCenter.x + 6.dp.toPx(), shieldCenter.y - 4.dp.toPx())
        }
        drawPath(vPath, color = Color(0xFF082B1B), style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round))
    }
}

private fun DrawScope.drawGeminiSparkle(
    center: Offset,
    radius: Float,
    primaryColor: Color,
    accentColor: Color
) {
    val path = Path().apply {
        // 4-point organic diamond star
        moveTo(center.x, center.y - radius)
        quadraticTo(center.x, center.y, center.x + radius, center.y)
        quadraticTo(center.x, center.y, center.x, center.y + radius)
        quadraticTo(center.x, center.y, center.x - radius, center.y)
        quadraticTo(center.x, center.y, center.x, center.y - radius)
        close()
    }

    drawPath(
        path = path,
        brush = Brush.radialGradient(
            colors = listOf(Color.White, accentColor, primaryColor),
            center = center,
            radius = radius
        ),
        style = Fill
    )
}

/**
 * Step 3: Animated Google Calendar Sync
 */
@Composable
fun CalendarSyncIllustration(
    modifier: Modifier = Modifier.fillMaxSize()
) {
    val transition = rememberInfiniteTransition(label = "calendar_sync")

    val syncRotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sync_spin"
    )

    val waveOffset by transition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cal_float"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val center = Offset(w / 2f, h / 2f)

        // Background calendar card (Cloud)
        val bgCardW = w * 0.55f
        val bgCardH = h * 0.55f
        val bgTopLeft = Offset(center.x - bgCardW * 0.35f, center.y - bgCardH * 0.65f - waveOffset)

        drawRoundRect(
            brush = Brush.linearGradient(
                colors = listOf(Color(0xFF10273D), Color(0xFF091624))
            ),
            topLeft = bgTopLeft,
            size = Size(bgCardW, bgCardH),
            cornerRadius = CornerRadius(20.dp.toPx(), 20.dp.toPx())
        )
        drawRoundRect(
            color = OceanBlue.copy(alpha = 0.5f),
            topLeft = bgTopLeft,
            size = Size(bgCardW, bgCardH),
            cornerRadius = CornerRadius(20.dp.toPx(), 20.dp.toPx()),
            style = Stroke(width = 1.5.dp.toPx())
        )

        // Foreground calendar card (Local Agenda)
        val fgCardW = w * 0.58f
        val fgCardH = h * 0.58f
        val fgTopLeft = Offset(center.x - fgCardW * 0.65f, center.y - fgCardH * 0.35f + waveOffset)

        drawRoundRect(
            brush = Brush.linearGradient(
                colors = listOf(Color(0xFF16324F), Color(0xFF0F2033))
            ),
            topLeft = fgTopLeft,
            size = Size(fgCardW, fgCardH),
            cornerRadius = CornerRadius(22.dp.toPx(), 22.dp.toPx())
        )
        drawRoundRect(
            color = OceanBlue,
            topLeft = fgTopLeft,
            size = Size(fgCardW, fgCardH),
            cornerRadius = CornerRadius(22.dp.toPx(), 22.dp.toPx()),
            style = Stroke(width = 2.dp.toPx())
        )

        // Calendar header bar on foreground card
        val headerH = fgCardH * 0.28f
        drawRoundRect(
            brush = Brush.horizontalGradient(
                colors = listOf(OceanBlue, AuroraCyan)
            ),
            topLeft = fgTopLeft,
            size = Size(fgCardW, headerH),
            cornerRadius = CornerRadius(22.dp.toPx(), 22.dp.toPx())
        )

        // Rings on calendar
        val ring1 = Offset(fgTopLeft.x + fgCardW * 0.30f, fgTopLeft.y)
        val ring2 = Offset(fgTopLeft.x + fgCardW * 0.70f, fgTopLeft.y)
        drawRoundRect(color = Color.White, topLeft = Offset(ring1.x - 3.dp.toPx(), ring1.y - 6.dp.toPx()), size = Size(6.dp.toPx(), 12.dp.toPx()), cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx()))
        drawRoundRect(color = Color.White, topLeft = Offset(ring2.x - 3.dp.toPx(), ring2.y - 6.dp.toPx()), size = Size(6.dp.toPx(), 12.dp.toPx()), cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx()))

        // Grid dots on calendar page
        val startGridX = fgTopLeft.x + fgCardW * 0.20f
        val startGridY = fgTopLeft.y + headerH + fgCardH * 0.18f
        val spacingX = fgCardW * 0.20f
        val spacingY = fgCardH * 0.16f

        for (r in 0..1) {
            for (c in 0..2) {
                val isTarget = (r == 1 && c == 1)
                drawCircle(
                    color = if (isTarget) MintPrimary else OceanBlue.copy(alpha = 0.4f),
                    radius = if (isTarget) 5.dp.toPx() else 3.dp.toPx(),
                    center = Offset(startGridX + c * spacingX, startGridY + r * spacingY)
                )
            }
        }

        // Circular Sync Hub between the two cards
        val syncCenter = Offset(center.x + fgCardW * 0.35f, center.y + fgCardH * 0.25f)
        drawCircle(color = Color(0xFF081B2E), radius = 22.dp.toPx(), center = syncCenter)
        drawCircle(color = OceanBlue, radius = 22.dp.toPx(), center = syncCenter, style = Stroke(width = 2.dp.toPx()))

        rotate(syncRotation, syncCenter) {
            // Two curved sync arrows
            val arcR = 13.dp.toPx()
            drawArc(
                color = AuroraCyan,
                startAngle = 30f,
                sweepAngle = 120f,
                useCenter = false,
                topLeft = Offset(syncCenter.x - arcR, syncCenter.y - arcR),
                size = Size(arcR * 2f, arcR * 2f),
                style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
            )
            drawArc(
                color = MintPrimary,
                startAngle = 210f,
                sweepAngle = 120f,
                useCenter = false,
                topLeft = Offset(syncCenter.x - arcR, syncCenter.y - arcR),
                size = Size(arcR * 2f, arcR * 2f),
                style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
            )
        }
    }
}

/**
 * Step 4: Animated Focus Velocity & Streak Analytics
 */
@Composable
fun VelocityAnalyticsIllustration(
    modifier: Modifier = Modifier.fillMaxSize()
) {
    val transition = rememberInfiniteTransition(label = "velocity_analytics")

    val needleAngle by transition.animateFloat(
        initialValue = -50f,
        targetValue = 50f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gauge_needle"
    )

    val bar1Height by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar_1"
    )

    val bar2Height by transition.animateFloat(
        initialValue = 0.70f,
        targetValue = 0.40f,
        animationSpec = infiniteRepeatable(
            animation = tween(1700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar_2"
    )

    val bar3Height by transition.animateFloat(
        initialValue = 0.50f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(1900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar_3"
    )

    val flamePulse by transition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flame_pulse"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val center = Offset(w / 2f, h / 2f)

        // Background container card
        val cardW = w * 0.72f
        val cardH = h * 0.72f
        val cardTopLeft = Offset(center.x - cardW / 2f, center.y - cardH / 2f)

        drawRoundRect(
            brush = Brush.linearGradient(
                colors = listOf(Color(0xFF2E1C07), Color(0xFF1B1104))
            ),
            topLeft = cardTopLeft,
            size = Size(cardW, cardH),
            cornerRadius = CornerRadius(26.dp.toPx(), 26.dp.toPx())
        )
        drawRoundRect(
            color = AmberGold.copy(alpha = 0.65f),
            topLeft = cardTopLeft,
            size = Size(cardW, cardH),
            cornerRadius = CornerRadius(26.dp.toPx(), 26.dp.toPx()),
            style = Stroke(width = 2.dp.toPx())
        )

        // Top Velocity Speedometer Arc
        val gaugeCenter = Offset(center.x, cardTopLeft.y + cardH * 0.42f)
        val gaugeR = cardW * 0.32f
        val arcRect = Offset(gaugeCenter.x - gaugeR, gaugeCenter.y - gaugeR)

        // Track Arc
        drawArc(
            color = Color(0xFF452B0E),
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = arcRect,
            size = Size(gaugeR * 2f, gaugeR * 2f),
            style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
        )

        // Active Gradient Arc
        val activeSweep = (needleAngle + 90f).coerceIn(0f, 180f)
        drawArc(
            brush = Brush.sweepGradient(
                listOf(AmberGold, CoralRed, MintPrimary)
            ),
            startAngle = 180f,
            sweepAngle = activeSweep,
            useCenter = false,
            topLeft = arcRect,
            size = Size(gaugeR * 2f, gaugeR * 2f),
            style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
        )

        // Needle
        rotate(needleAngle, gaugeCenter) {
            val needleLen = gaugeR * 0.78f
            drawLine(
                brush = Brush.verticalGradient(listOf(AmberGold, Color.White)),
                start = gaugeCenter,
                end = Offset(gaugeCenter.x, gaugeCenter.y - needleLen),
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawCircle(color = Color.White, radius = 4.dp.toPx(), center = gaugeCenter)
        }

        // Analytics Bar Columns at bottom
        val barBottom = cardTopLeft.y + cardH * 0.88f
        val maxBarH = cardH * 0.26f
        val barW = 12.dp.toPx()
        val barSpacing = 18.dp.toPx()
        val startBarX = center.x - barSpacing

        // Bar 1
        val h1 = maxBarH * bar1Height
        drawRoundRect(
            color = AmberGold,
            topLeft = Offset(startBarX - barW / 2f, barBottom - h1),
            size = Size(barW, h1),
            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
        )

        // Bar 2
        val h2 = maxBarH * bar2Height
        drawRoundRect(
            color = CoralRed,
            topLeft = Offset(center.x - barW / 2f, barBottom - h2),
            size = Size(barW, h2),
            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
        )

        // Bar 3
        val h3 = maxBarH * bar3Height
        drawRoundRect(
            color = MintPrimary,
            topLeft = Offset(center.x + barSpacing - barW / 2f, barBottom - h3),
            size = Size(barW, h3),
            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
        )

        // Streak Fire Spark in Top Right
        val sparkCenter = Offset(cardTopLeft.x + cardW * 0.84f, cardTopLeft.y + cardH * 0.20f)
        scale(flamePulse, flamePulse, sparkCenter) {
            drawCircle(color = AmberGold, radius = 7.dp.toPx(), center = sparkCenter)
            drawCircle(color = CoralRed, radius = 4.dp.toPx(), center = sparkCenter)
        }
    }
}
