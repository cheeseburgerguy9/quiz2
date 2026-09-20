package com.aistudio.caitlindaily.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SettingsSuggest
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.caitlindaily.data.model.TimetableSlot
import com.aistudio.caitlindaily.ui.components.HeaderTimeArtifact
import com.aistudio.caitlindaily.ui.theme.AmberGold
import com.aistudio.caitlindaily.ui.theme.CoralRed
import com.aistudio.caitlindaily.ui.theme.ForestBackground
import com.aistudio.caitlindaily.ui.theme.ForestSurfaceBorder
import com.aistudio.caitlindaily.ui.theme.ForestSurfaceCard
import com.aistudio.caitlindaily.ui.theme.MintPrimary
import com.aistudio.caitlindaily.ui.theme.MintPrimaryDark
import com.aistudio.caitlindaily.ui.theme.OceanBlue
import com.aistudio.caitlindaily.ui.theme.TextPrimary
import com.aistudio.caitlindaily.ui.theme.TextSecondary
import com.aistudio.caitlindaily.ui.theme.TextTertiary

@Composable
fun TimetableScreen(
    slots: List<TimetableSlot>,
    isGeminiAvailable: Boolean,
    onOptimizeSchedule: () -> Unit,
    onGoToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = ForestBackground
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 96.dp)
        ) {
            item {
                Column {
                    Text(
                        text = "Timetable & Flow",
                        color = TextPrimary,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Dynamic chrono-structured energy alignment",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Time artifact hero
                HeaderTimeArtifact()

                Spacer(modifier = Modifier.height(20.dp))

                // AI Schedule Optimization Button
                Button(
                    onClick = {
                        if (isGeminiAvailable) {
                            onOptimizeSchedule()
                        } else {
                            onGoToSettings()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isGeminiAvailable) MintPrimary else ForestSurfaceBorder,
                        contentColor = if (isGeminiAvailable) MintPrimaryDark else TextTertiary
                    ),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("optimize_timetable_btn")
                ) {
                    Icon(
                        imageVector = if (isGeminiAvailable) Icons.Default.AutoAwesome else Icons.Default.Key,
                        contentDescription = null,
                        tint = if (isGeminiAvailable) MintPrimaryDark else AmberGold,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isGeminiAvailable) "Gemini AI Optimize Schedule" else "Add API Key to Enable AI Optimization",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Daily Chrono Slots",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            items(slots) { slot ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(ForestSurfaceCard)
                        .border(1.dp, ForestSurfaceBorder, RoundedCornerShape(20.dp))
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MintPrimary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = MintPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = slot.timeLabel,
                                color = MintPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = slot.taskTitle ?: "Free Focus Slot",
                                color = TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val categoryName = slot.category ?: "Focus"
                                val catColor = when (categoryName.lowercase()) {
                                    "work" -> OceanBlue
                                    "study" -> AmberGold
                                    "health" -> MintPrimary
                                    else -> CoralRed
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .background(catColor.copy(alpha = 0.15f))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = categoryName,
                                        color = catColor,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                if (slot.isAiOptimized) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(50))
                                            .background(AmberGold.copy(alpha = 0.15f))
                                            .border(1.dp, AmberGold.copy(alpha = 0.4f), RoundedCornerShape(50))
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.AutoAwesome,
                                                contentDescription = null,
                                                tint = AmberGold,
                                                modifier = Modifier.size(10.dp)
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = "AI Arranged",
                                                color = AmberGold,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}
