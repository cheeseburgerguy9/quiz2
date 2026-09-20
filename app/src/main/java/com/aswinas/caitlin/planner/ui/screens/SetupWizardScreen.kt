package com.aswinas.caitlin.planner.ui.screens

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
import androidx.compose.material3.MaterialTheme
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
import com.aswinas.caitlin.planner.ui.components.WizardAnimatedIllustration
import com.aswinas.caitlin.planner.ui.theme.AmberGold
import com.aswinas.caitlin.planner.ui.theme.AsymmetricalCardShape
import com.aswinas.caitlin.planner.ui.theme.ElectricViolet
import com.aswinas.caitlin.planner.ui.theme.OceanBlue
import com.aswinas.caitlin.planner.ui.theme.WizardHeroShape

data class WizardStep(
    val stepNumber: Int,
    val title: String,
    val subtitle: String,
    val description: String,
    val icon: ImageVector,
    val iconColor: Color,
    val containerColor: Color,
    val shapeType: Int // 0: Pill/Squircle, 1: Asymmetrical, 2: Rounded, 3: WizardHero
)

@Composable
fun SetupWizardScreen(
    onFinish: (userName: String, age: Int) -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentStepIndex by remember { mutableIntStateOf(0) }
    var enteredName by remember { mutableStateOf("") }
    var enteredAge by remember { mutableStateOf("") }

    val primaryColor = MaterialTheme.colorScheme.primary
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer

    val steps = remember(primaryColor, primaryContainer) {
        listOf(
            WizardStep(
                stepNumber = 1,
                title = "Welcome to Daily Agenda",
                subtitle = "Material 3 Expressive",
                description = "Experience a vibrant productivity companion crafted with expressive shapes, fluid animations, and intelligent scheduling.",
                icon = Icons.Default.FolderSpecial,
                iconColor = primaryColor,
                containerColor = primaryContainer.copy(alpha = 0.6f),
                shapeType = 3
            ),
            WizardStep(
                stepNumber = 2,
                title = "AI Vision & Verification",
                subtitle = "Powered by Gemini 3.8 Flash",
                description = "Verify your high-priority tasks with screenshot proof. Let Gemini 3.8 Flash extract study durations and analyze your progress.",
                icon = Icons.Default.VerifiedUser,
                iconColor = ElectricViolet,
                containerColor = ElectricViolet.copy(alpha = 0.2f),
                shapeType = 1
            ),
            WizardStep(
                stepNumber = 3,
                title = "Google Calendar Sync",
                subtitle = "Two-Way Integration",
                description = "Seamlessly sync and schedule daily study sessions directly to your device calendar provider or system agenda with one tap.",
                icon = Icons.Default.CalendarMonth,
                iconColor = OceanBlue,
                containerColor = OceanBlue.copy(alpha = 0.2f),
                shapeType = 2
            ),
            WizardStep(
                stepNumber = 4,
                title = "Velocity & Focus Insights",
                subtitle = "Private & Adaptive",
                description = "Track your focus velocities, active streaks, and habit momentum with local Room persistence and real-time Material 3 dynamic theming.",
                icon = Icons.Default.ShowChart,
                iconColor = AmberGold,
                containerColor = AmberGold.copy(alpha = 0.2f),
                shapeType = 0
            )
        )
    }

    val currentStep = steps[currentStepIndex]

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar with back button, step indicator, and skip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (currentStepIndex > 0) {
                    IconButton(
                        onClick = { currentStepIndex-- },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.size(40.dp))
                }

                // Step count pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f), RoundedCornerShape(50))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Step ${currentStepIndex + 1} of ${steps.size}",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Text(
                    text = "Skip",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                    // Material 3 Expressive Animated Illustration
                    Box(
                        modifier = Modifier
                            .size(175.dp)
                            .testTag("wizard_illustration_${step.stepNumber}"),
                        contentAlignment = Alignment.Center
                    ) {
                        WizardAnimatedIllustration(
                            stepIndex = step.stepNumber - 1,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

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
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = step.description,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    // Interactive profile setup on the first step
                    if (step.stepNumber == 1) {
                        Spacer(modifier = Modifier.height(18.dp))
                        OutlinedTextField(
                            value = enteredName,
                            onValueChange = { enteredName = it },
                            label = { Text("Your Preferred Name", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                            placeholder = { Text("Enter your name", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier
                                .fillMaxWidth(0.90f)
                                .testTag("wizard_name_input")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = enteredAge,
                            onValueChange = { enteredAge = it.filter { c -> c.isDigit() }.take(3) },
                            label = { Text("Your Age (Optional)", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                            placeholder = { Text("e.g. 24", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier
                                .fillMaxWidth(0.90f)
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
                                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
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
                        val ageInt = enteredAge.toIntOrNull() ?: 0
                        onFinish(enteredName.trim(), ageInt)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
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
