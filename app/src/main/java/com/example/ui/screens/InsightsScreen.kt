package com.example.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StudySessionEntity
import com.example.ui.components.ProgressGraph
import com.example.ui.theme.AmberGold
import com.example.ui.theme.ForestBackground
import com.example.ui.theme.ForestSurfaceBorder
import com.example.ui.theme.ForestSurfaceCard
import com.example.ui.theme.MintPrimary
import com.example.ui.theme.MintPrimaryDark
import com.example.ui.theme.OceanBlue
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@Composable
fun InsightsScreen(totalTasks: Int, completedTasks: Int, aiVerifiedTasks: Int, studySessions: List<StudySessionEntity>, aiAvailable: Boolean, aiInsights: String, aiBusy: Boolean, onGenerateInsights: () -> Unit, modifier: Modifier = Modifier) {
    Surface(modifier.fillMaxSize(), color = ForestBackground) {
        if (!aiAvailable) Box(Modifier.fillMaxSize().padding(24.dp), Alignment.Center) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Default.AutoAwesome, null, tint = TextTertiary, modifier = Modifier.size(44.dp)); Spacer(Modifier.height(12.dp)); Text("Gemini is not configured", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold); Text("Add your own Gemini API key in Settings to generate AI insights.", color = TextSecondary, fontSize = 14.sp) } }
        else LazyColumn(Modifier.fillMaxSize().statusBarsPadding(), contentPadding = PaddingValues(20.dp, 16.dp, 20.dp, 96.dp)) {
            item {
                Text("Productivity Insights", color = TextPrimary, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp)); Text("Real-time velocity and focus execution", color = TextSecondary, fontSize = 14.sp)
                Spacer(Modifier.height(20.dp))
                Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(ForestSurfaceCard).border(1.dp, ForestSurfaceBorder, RoundedCornerShape(24.dp)).padding(20.dp)) { Row(verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(46.dp).clip(CircleShape).background(AmberGold.copy(alpha=.2f)), Alignment.Center) { Icon(Icons.Default.LocalFireDepartment, null, tint = AmberGold, modifier = Modifier.size(28.dp)) }; Spacer(Modifier.width(14.dp)); Column { Text("Productivity at a glance", color = TextPrimary, fontSize = 17.sp, fontWeight = FontWeight.Bold); Text("Gemini organizes tasks, completions and study time below.", color = TextSecondary, fontSize = 12.sp) } } }
                Spacer(Modifier.height(18.dp)); ProgressGraph(); Spacer(Modifier.height(20.dp))
                Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(12.dp)) { StatCard("Tasks Logged", totalTasks.toString(), Icons.Default.TaskAlt, MintPrimary, Modifier.weight(1f)); StatCard("Completion", if (totalTasks > 0) "${(completedTasks * 100) / totalTasks}%" else "0%", Icons.Default.ShowChart, OceanBlue, Modifier.weight(1f)); StatCard("AI Verified", aiVerifiedTasks.toString(), Icons.Default.AutoAwesome, AmberGold, Modifier.weight(1f)) }
                Spacer(Modifier.height(18.dp))
                val studyMinutes = studySessions.sumOf { it.minutes }
                Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(ForestSurfaceCard).border(1.dp, ForestSurfaceBorder, RoundedCornerShape(20.dp)).padding(18.dp)) { Column { Text("Study Time", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold); Spacer(Modifier.height(5.dp)); Text("$studyMinutes minutes recorded from verified screenshots", color = TextSecondary, fontSize = 13.sp); if (studySessions.isNotEmpty()) { Spacer(Modifier.height(10.dp)); studySessions.take(5).forEach { Text("• ${it.appName}: ${it.minutes} min", color = TextSecondary, fontSize = 12.sp) } } } }
                Spacer(Modifier.height(18.dp))
                Button(onClick = onGenerateInsights, enabled = !aiBusy, colors = ButtonDefaults.buttonColors(containerColor = MintPrimary, contentColor = MintPrimaryDark), shape = RoundedCornerShape(50), modifier = Modifier.fillMaxWidth().height(48.dp)) { Icon(Icons.Default.AutoAwesome, null); Spacer(Modifier.width(8.dp)); Text(if (aiBusy) "Organizing with Gemini…" else "Generate AI Suggestions", fontWeight = FontWeight.Bold) }
                if (aiInsights.isNotBlank()) { Spacer(Modifier.height(14.dp)); Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(ForestSurfaceCard).border(1.dp, ForestSurfaceBorder, RoundedCornerShape(20.dp)).padding(18.dp)) { Column { Text("Gemini Suggestions", color = MintPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold); Spacer(Modifier.height(10.dp)); Text(aiInsights, color = TextSecondary, fontSize = 13.sp, lineHeight = 19.sp) } } }
            }
        }
    }
}

@Composable private fun StatCard(title: String, value: String, icon: ImageVector, tint: androidx.compose.ui.graphics.Color, modifier: Modifier = Modifier) { Box(modifier.clip(RoundedCornerShape(20.dp)).background(ForestSurfaceCard).border(1.dp, ForestSurfaceBorder, RoundedCornerShape(20.dp)).padding(14.dp)) { Column { Icon(icon, null, tint = tint, modifier = Modifier.size(20.dp)); Spacer(Modifier.height(10.dp)); Text(value, color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold); Spacer(Modifier.height(2.dp)); Text(title, color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium) } } }
