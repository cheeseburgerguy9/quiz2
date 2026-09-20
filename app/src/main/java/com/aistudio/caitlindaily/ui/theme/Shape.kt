package com.aistudio.caitlindaily.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// Material You Expressive Shapes
val MaterialYouShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

// Distinct Material You decorative shapes for Setup Wizard and cards
val PillShape = RoundedCornerShape(50)
val SquircleShape = RoundedCornerShape(22.dp)
val AsymmetricalCardShape = RoundedCornerShape(
    topStart = 28.dp,
    topEnd = 14.dp,
    bottomEnd = 28.dp,
    bottomStart = 14.dp
)
val WizardHeroShape = RoundedCornerShape(
    topStart = 36.dp,
    topEnd = 36.dp,
    bottomEnd = 16.dp,
    bottomStart = 36.dp
)
