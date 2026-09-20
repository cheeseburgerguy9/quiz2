package com.aswinas.caitlin.planner.ui.theme

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

// Distinct Material 3 Expressive decorative shapes
val PillShape = RoundedCornerShape(50)
val SquircleShape = RoundedCornerShape(24.dp)
val ExpressivePill = RoundedCornerShape(999.dp)
val ExpressiveCardShape = RoundedCornerShape(28.dp)
val ExpressiveLargeCardShape = RoundedCornerShape(32.dp)
val AsymmetricalCardShape = RoundedCornerShape(
    topStart = 32.dp,
    topEnd = 16.dp,
    bottomEnd = 32.dp,
    bottomStart = 16.dp
)

val WizardHeroShape = RoundedCornerShape(
    topStart = 40.dp,
    topEnd = 40.dp,
    bottomEnd = 18.dp,
    bottomStart = 40.dp
)
