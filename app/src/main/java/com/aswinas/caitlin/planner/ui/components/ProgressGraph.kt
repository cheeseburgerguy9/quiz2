package com.aswinas.caitlin.planner.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aswinas.caitlin.planner.ui.theme.AmberGold
import com.aswinas.caitlin.planner.ui.theme.CoralRed
import com.aswinas.caitlin.planner.ui.theme.EmeraldGreen

@Composable
fun ProgressGraph(
    completionRates: List<Float> = listOf(0.75f, 0.88f, 0.40f, 0.95f, 0.55f, 0.35f, 0.82f),
    dayLabels: List<String> = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"),
    currentDayIndex: Int = 4, // Friday default
    modifier: Modifier = Modifier
) {
    val outlineColor = MaterialTheme.colorScheme.outline
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant

    // Calculate average completion
    val avgRate = if (completionRates.isNotEmpty()) {
        (completionRates.sum() / completionRates.size) * 100f
    } else 70f
    val isOverallPaceFast = avgRate >= 65f

    // Dynamic Color definitions:
    // Greener shade for increased or faster completion (>= 70%)
    val fastGreenTop = Color(0xFF00E676)
    val fastGreenBottom = Color(0xFF00C853)

    // Moderate/steady yellow-amber shade (45% - 69%)
    val steadyYellowTop = Color(0xFFFFD54F)
    val steadyYellowBottom = Color(0xFFFFB300)

    // Yellow to reddish shade for decreased or slow completion (< 45%)
    val slowReddishTop = Color(0xFFFF8A65)
    val slowReddishBottom = Color(0xFFFF3D00)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(surfaceVariantColor.copy(alpha = 0.65f))
            .border(1.dp, outlineColor.copy(alpha = 0.25f), RoundedCornerShape(26.dp))
            .padding(20.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Weekly Focus Velocity",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Average completion ${avgRate.toInt()}%",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }

                // Dynamic velocity badge: Green if increased/faster, Reddish/Yellow if slower/decreased
                val badgeBg = if (isOverallPaceFast) fastGreenBottom.copy(alpha = 0.15f) else slowReddishBottom.copy(alpha = 0.15f)
                val badgeTextColor = if (isOverallPaceFast) Color(0xFF00C853) else Color(0xFFE53935)
                val badgeText = if (isOverallPaceFast) "↑ Faster (+18%)" else "↓ Slower (-12%)"

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(badgeBg)
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = badgeText,
                        color = badgeTextColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Bar Chart Canvas with Dynamic Color gradients
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            ) {
                val barWidth = 24.dp.toPx()
                val totalWidth = size.width
                val totalHeight = size.height
                val count = completionRates.size
                val spacing = (totalWidth - (barWidth * count)) / (count + 1)

                // Background horizontal guide lines
                for (i in 1..3) {
                    val y = totalHeight * (i * 0.25f)
                    drawLine(
                        color = outlineColor.copy(alpha = 0.15f),
                        start = Offset(0f, y),
                        end = Offset(totalWidth, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                // Draw bars with dynamic coloring
                completionRates.forEachIndexed { index, rate ->
                    val x = spacing + index * (barWidth + spacing)
                    val barHeight = totalHeight * rate.coerceIn(0.12f, 1.0f)
                    val y = totalHeight - barHeight

                    // Bar track background
                    drawRoundRect(
                        color = outlineColor.copy(alpha = 0.12f),
                        topLeft = Offset(x, 0f),
                        size = Size(barWidth, totalHeight),
                        cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
                    )

                    // Dynamic colors based on rate:
                    // >= 0.70f: Greener shade (increased/fast)
                    // 0.45f .. 0.69f: Yellow-Amber shade (steady)
                    // < 0.45f: Yellow to Reddish shade (decreased/slow)
                    val (colorTop, colorBottom) = when {
                        rate >= 0.70f -> Pair(fastGreenTop, fastGreenBottom)
                        rate >= 0.45f -> Pair(steadyYellowTop, steadyYellowBottom)
                        else -> Pair(slowReddishTop, slowReddishBottom)
                    }

                    // Filled active bar
                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(colorTop, colorBottom),
                            startY = y,
                            endY = totalHeight
                        ),
                        topLeft = Offset(x, y),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Day labels row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                dayLabels.forEachIndexed { index, label ->
                    val isToday = index == currentDayIndex
                    Text(
                        text = label,
                        color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        fontSize = 11.sp,
                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Dynamic color legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Greener = Fast/Increased
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(fastGreenBottom))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Fast / High (≥70%)", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                // Yellow/Amber = Steady
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(steadyYellowBottom))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Steady (45–69%)", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                // Reddish = Slower/Decreased
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(slowReddishBottom))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Slow (<45%)", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
