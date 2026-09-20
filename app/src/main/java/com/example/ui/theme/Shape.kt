package com.example.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// Android 16/17 Expressive Shapes: Fluid super-ellipses & pill forms
val ExpressiveShapes = Shapes(
    extraSmall = RoundedCornerShape(12.dp),
    small = RoundedCornerShape(16.dp),
    medium = RoundedCornerShape(24.dp),
    large = RoundedCornerShape(32.dp),
    extraLarge = RoundedCornerShape(36.dp)
)

val PillShape = CircleShape
val ExpressiveCardShape = RoundedCornerShape(28.dp)
val ExpressiveSheetShape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
val ExpressiveChipShape = RoundedCornerShape(16.dp)
val ExpressiveButtonShape = RoundedCornerShape(20.dp)
