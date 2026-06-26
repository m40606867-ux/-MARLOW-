package com.example.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush

data class VibeThemeColors(
    val primaryColor: Color,
    val secondaryColor: Color,
    val backgroundColor: Color,
    val cardBackground: Color,
    val textOnPrimary: Color,
    val textOnBackground: Color,
    val gradientBrush: Brush
)

object VibeThemeRegistry {
    val BlueVibe = VibeThemeColors(
        primaryColor = Color(0xFF0288D1),
        secondaryColor = Color(0xFFB3E5FC),
        backgroundColor = Color(0xFFF2F9FF),
        cardBackground = Color(0xFFE1F5FE),
        textOnPrimary = Color.White,
        textOnBackground = Color(0xFF01579B),
        gradientBrush = Brush.verticalGradient(
            colors = listOf(Color(0xFFE0F7FA), Color(0xFFB3E5FC), Color(0xFFE1F5FE))
        )
    )

    val OrangeVibe = VibeThemeColors(
        primaryColor = Color(0xFFF57C00),
        secondaryColor = Color(0xFFFFE0B2),
        backgroundColor = Color(0xFFFFF9E6),
        cardBackground = Color(0xFFFFF3E0),
        textOnPrimary = Color.White,
        textOnBackground = Color(0xFFE65100),
        gradientBrush = Brush.verticalGradient(
            colors = listOf(Color(0xFFFFFDE7), Color(0xFFFFE0B2), Color(0xFFFFF3E0))
        )
    )

    val GreenVibe = VibeThemeColors(
        primaryColor = Color(0xFF388E3C),
        secondaryColor = Color(0xFFC8E6C9),
        backgroundColor = Color(0xFFF1F8E9),
        cardBackground = Color(0xFFE8F5E9),
        textOnPrimary = Color.White,
        textOnBackground = Color(0xFF1B5E20),
        gradientBrush = Brush.verticalGradient(
            colors = listOf(Color(0xFFF1F8E9), Color(0xFFC8E6C9), Color(0xFFE8F5E9))
        )
    )

    val PurpleVibe = VibeThemeColors(
        primaryColor = Color(0xFF7B1FA2),
        secondaryColor = Color(0xFFE1BEE7),
        backgroundColor = Color(0xFFFAF2FB),
        cardBackground = Color(0xFFF3E5F5),
        textOnPrimary = Color.White,
        textOnBackground = Color(0xFF4A148C),
        gradientBrush = Brush.verticalGradient(
            colors = listOf(Color(0xFFF9F0FC), Color(0xFFE1BEE7), Color(0xFFF3E5F5))
        )
    )

    val PinkVibe = VibeThemeColors(
        primaryColor = Color(0xFFD81B60),
        secondaryColor = Color(0xFFF8BBD0),
        backgroundColor = Color(0xFFFFEFF4),
        cardBackground = Color(0xFFFFEBEE),
        textOnPrimary = Color.White,
        textOnBackground = Color(0xFF880E4F),
        gradientBrush = Brush.verticalGradient(
            colors = listOf(Color(0xFFFFEFF4), Color(0xFFF8BBD0), Color(0xFFFFEBEE))
        )
    )

    val GreyVibe = VibeThemeColors(
        primaryColor = Color(0xFF616161),
        secondaryColor = Color(0xFFE0E0E0),
        backgroundColor = Color(0xFFFAFAFA),
        cardBackground = Color(0xFFF5F5F5),
        textOnPrimary = Color.White,
        textOnBackground = Color(0xFF212121),
        gradientBrush = Brush.verticalGradient(
            colors = listOf(Color(0xFFECEFF1), Color(0xFFCFD8DC), Color(0xFFECEFF1))
        )
    )

    fun getTheme(themeName: String?): VibeThemeColors {
        return when (themeName?.lowercase()?.trim()) {
            "blue" -> BlueVibe
            "orange" -> OrangeVibe
            "green" -> GreenVibe
            "purple" -> PurpleVibe
            "pink" -> PinkVibe
            else -> GreyVibe
        }
    }
}
