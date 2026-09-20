package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberGold
import com.example.ui.theme.AmberGoldContainer
import com.example.ui.theme.AsymmetricalCardShape
import com.example.ui.theme.ForestBackground
import com.example.ui.theme.ForestSurfaceBorder
import com.example.ui.theme.ForestSurfaceCard
import com.example.ui.theme.MintPrimary
import com.example.ui.theme.MintPrimaryContainer
import com.example.ui.theme.MintPrimaryDark
import com.example.ui.theme.OceanBlue
import com.example.ui.theme.OceanBlueContainer
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WizardHeroShape

data class WizardStep(
    val stepNumber: Int,
    val title: String,
    val subtitle: String,
    val description: String,
    val icon: ImageVector,
    val iconColor: Color,
    val containerColor: Color,
    val shapeType: Int // 0: Pill, 1: Asymmetrical, 2: Squircle, 3: WizardHero
)

@Composable
fun SetupWizardScreen(
    onFinish: (userName: String, age: Int) -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentStepIndex by remember { mutableIntStateOf(0) }
    var enteredName by remember { mutableStateOf("Aswin") }
    var enteredAge by remember { mutableStateOf("22") }

    val steps = remember {
        listOf(
            WizardStep(
                stepNumber = 1,
                title = "Welcome to Caitlin Daily",
                subtitle = "Your AI Daily Tracker",
                description = "Experience mindful productivity designed with Material You elegance. Harmonize tasks, timetable scheduling, and smart daily agendas in one seamless flow.",
                icon = Icons.Default.AutoAwesome,
                iconColor = MintPrimary,
                containerColor = MintPrimaryContainer,
                shapeType = 3
            ),
            WizardStep(
                stepNumber = 2,
                title = "Smart Agendas & Priorities",
                subtitle = "Categorize What Truly Matters",
                description = "Organize tasks by Work, Study, Health, and Personal life. Assign High, Medium, or Low priority badges to ensure your focus is always directed on top outcomes.",
                icon = Icons.Default.FolderSpecial,
                iconColor = OceanBlue,
                containerColor = OceanBlueContainer,
                shapeType = 1
            ),
            WizardStep(
                stepNumber = 3,
                title = "AI Timetable Optimization",
                subtitle = "Gemini Powered Chrono-Flow",
                description = "Synchronize your schedule with Google Calendar and allow Gemini AI to automatically arrange high-priority objectives into your peak cognitive energy slots.",
                icon = Icons.Default.CalendarMonth,
                iconColor = AmberGold,
                containerColor = AmberGoldContainer,
                shapeType = 2
            ),
            WizardStep(
                stepNumber = 4,
                title = "AI Task Verification",
                subtitle = "Authentic Proof & Accountability",
                description = "Submit completed objectives with notes and screenshot evidence. Gemini AI reviews your progress, stamps tasks as AI Verified, and awards accountability points.",
                icon = Icons.Default.VerifiedUser,
                iconColor = MintPrimary,
                containerColor = MintPrimaryContainer,
                shapeType = 1
            ),
            WizardStep(
                stepNumber = 5,
                title = "Insights & Streaks",
                subtitle = "Data-Driven Momentum",
                description = "Visualize weekly focus velocity with responsive graphs, track category breakdown percentages, and sustain your consecutive day streaks.",
                icon = Icons.Default.ShowChart,
                iconColor = OceanBlue,
                containerColor = OceanBlueContainer,
                shapeType = 3
            )
        )
    }

    val currentStep = steps[currentStepIndex]

    Surface(
        modifier = modifier.fillMaxSize(),
        color = ForestBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar: Back & Skip button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (currentStepIndex > 0) {
                    IconButton(
                        onClick = { currentStepIndex-- },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(ForestSurfaceCard)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.size(44.dp))
                }

                // Step count pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(ForestSurfaceCard)
                        .border(1.dp, ForestSurfaceBorder, RoundedCornerShape(50))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Step ${currentStepIndex + 1} of ${steps.size}",
                        color = MintPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Text(
                    text = "Skip",
                    color = TextSecondary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable { onSkip() }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .testTag("wizard_skip_btn")
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Animated Step Content
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "wizard_step_anim",
                modifier = Modifier.weight(1f)
            ) { step ->
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Material You Expressive Container with Graphic
                    val stepShape = when (step.shapeType) {
                        1 -> AsymmetricalCardShape
                        2 -> RoundedCornerShape(26.dp)
                        3 -> WizardHeroShape
                        else -> RoundedCornerShape(32.dp)
                    }

                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .clip(stepShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        step.containerColor,
                                        ForestSurfaceCard
                                    )
                                )
                            )
                            .border(2.dp, step.iconColor.copy(alpha = 0.4f), stepShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = step.icon,
                            contentDescription = step.title,
                            tint = step.iconColor,
                            modifier = Modifier.size(68.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        text = step.subtitle.uppercase(),
                        color = step.iconColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = step.title,
                        color = TextPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = step.description,
                        color = TextSecondary,
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    // Interactive offline profile setup on the first step
                    if (step.stepNumber == 1) {
                        Spacer(modifier = Modifier.height(20.dp))
                        OutlinedTextField(
                            value = enteredName,
                            onValueChange = { enteredName = it },
                            label = { Text("Your Preferred Name", color = TextSecondary) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedBorderColor = MintPrimary,
                                unfocusedBorderColor = ForestSurfaceBorder,
                                focusedContainerColor = ForestSurfaceCard,
                                unfocusedContainerColor = ForestSurfaceCard
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .testTag("wizard_name_input")
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = enteredAge,
                            onValueChange = { enteredAge = it.filter { c -> c.isDigit() }.take(3) },
                            label = { Text("Your Age", color = TextSecondary) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedBorderColor = MintPrimary,
                                unfocusedBorderColor = ForestSurfaceBorder,
                                focusedContainerColor = ForestSurfaceCard,
                                unfocusedContainerColor = ForestSurfaceCard
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .testTag("wizard_age_input")
                        )
                    }
                }
            }

            // Indicator Dots / Expanded Pills
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                steps.forEachIndexed { index, _ ->
                    val isSelected = index == currentStepIndex
                    val pillWidth by animateDpAsState(
                        targetValue = if (isSelected) 32.dp else 10.dp,
                        animationSpec = spring(),
                        label = "indicator_pill_width"
                    )
                    Box(
                        modifier = Modifier
                            .height(10.dp)
                            .width(pillWidth)
                            .clip(RoundedCornerShape(50))
                            .background(
                                if (isSelected) MintPrimary else ForestSurfaceBorder
                            )
                    )
                }
            }

            // Next / Get Started Action Button
            Button(
                onClick = {
                    if (currentStepIndex < steps.size - 1) {
                        currentStepIndex++
                    } else {
                        val ageInt = enteredAge.toIntOrNull() ?: 22
                        onFinish(enteredName.ifBlank { "User" }, ageInt)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MintPrimary,
                    contentColor = MintPrimaryDark
                ),
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("wizard_action_btn")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = if (currentStepIndex == steps.size - 1) "Get Started" else "Next",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = if (currentStepIndex == steps.size - 1) Icons.Default.CheckCircle else Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
