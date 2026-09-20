package com.example.ui.components

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ForestSurfaceBorder
import com.example.ui.theme.ForestSurfaceCard
import com.example.ui.theme.MintPrimary
import com.example.ui.theme.OceanBlue
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@Composable
fun ProgressGraph(
    completionRates: List<Float> = listOf(0.7f, 0.85f, 0.6f, 0.95f, 0.8f, 0.9f, 1.0f),
    dayLabels: List<String> = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"),
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(ForestSurfaceCard)
            .border(1.dp, ForestSurfaceBorder, RoundedCornerShape(24.dp))
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
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Average completion 84%",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(MintPrimary.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "+18% vs last week",
                        color = MintPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Bar Chart Canvas
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

                // Draw background horizontal guide lines
                for (i in 1..3) {
                    val y = totalHeight * (i * 0.25f)
                    drawLine(
                        color = ForestSurfaceBorder.copy(alpha = 0.5f),
                        start = Offset(0f, y),
                        end = Offset(totalWidth, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                // Draw bars
                completionRates.forEachIndexed { index, rate ->
                    val x = spacing + index * (barWidth + spacing)
                    val barHeight = totalHeight * rate.coerceIn(0.1f, 1.0f)
                    val y = totalHeight - barHeight

                    // Bar track background
                    drawRoundRect(
                        color = ForestSurfaceBorder.copy(alpha = 0.35f),
                        topLeft = Offset(x, 0f),
                        size = Size(barWidth, totalHeight),
                        cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
                    )

                    // Filled active bar
                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MintPrimary,
                                OceanBlue
                            ),
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
                dayLabels.forEach { label ->
                    Text(
                        text = label,
                        color = if (label == "Fri") MintPrimary else TextTertiary,
                        fontSize = 11.sp,
                        fontWeight = if (label == "Fri") FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}
