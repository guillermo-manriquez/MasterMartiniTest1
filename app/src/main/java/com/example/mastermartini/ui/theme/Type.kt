package com.example.mastermartini.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontWeight

// mastermartini.cl uses Roboto, which is already the Android system font,
// so the Material 3 defaults are kept and only heading weights are raised.
private val Base = Typography()

val Typography = Typography(
    headlineMedium = Base.headlineMedium.copy(fontWeight = FontWeight.SemiBold),
    headlineSmall = Base.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
    titleLarge = Base.titleLarge.copy(fontWeight = FontWeight.SemiBold),
    titleMedium = Base.titleMedium.copy(fontWeight = FontWeight.SemiBold),
)
