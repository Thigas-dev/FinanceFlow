package com.financeflow.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

/** Paleta "Emerald Ledger" (emerald_ledger/DESIGN.md). */
object FF {
    val Ink = Color(0xFF0B0F19)
    val Surface1 = Color(0xFF0F172A)
    val Surface2 = Color(0xFF1E293B)
    val SurfacePressed = Color(0xFF283548)
    val Border = Color(0xFF334155)
    val Emerald = Color(0xFF10B981)
    val EmeraldDark = Color(0xFF059669)
    val EmeraldLight = Color(0xFF4EDEA3)
    val Sky = Color(0xFF38BDF8)
    val Crimson = Color(0xFFF43F5E)
    val CrimsonLight = Color(0xFFFF7886)
    val Amber = Color(0xFFF59E0B)
    val Violet = Color(0xFF8B5CF6)
    val TextPrimary = Color(0xFFF8FAFC)
    val TextSecondary = Color(0xFF94A3B8)
    val TextTertiary = Color(0xFF64748B)

    val palette = listOf(
        0xFF10B981, 0xFF38BDF8, 0xFF8B5CF6, 0xFFF59E0B, 0xFFF43F5E, 0xFFEC4899, 0xFF14B8A6, 0xFF6366F1, 0xFFF97316, 0xFF94A3B8,
    )
}

private val Tnum = "tnum"

private val FFTypography = Typography(
    displaySmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 32.sp, lineHeight = 40.sp, letterSpacing = (-0.02).em, fontFeatureSettings = Tnum),
    headlineLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 28.sp, lineHeight = 36.sp, letterSpacing = (-0.015).em),
    headlineMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, lineHeight = 28.sp, letterSpacing = (-0.01).em),
    headlineSmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, lineHeight = 24.sp),
    titleLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, lineHeight = 26.sp),
    titleMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 22.sp),
    titleSmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp, fontFeatureSettings = Tnum),
    bodyLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 12.sp, lineHeight = 16.sp),
    labelLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.01.em, fontFeatureSettings = Tnum),
    labelMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.02.em),
    labelSmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 10.sp, lineHeight = 14.sp, letterSpacing = 0.05.em),
)

private val FFColors = darkColorScheme(
    primary = FF.Emerald,
    onPrimary = FF.Ink,
    primaryContainer = FF.EmeraldDark,
    onPrimaryContainer = FF.TextPrimary,
    secondary = FF.Sky,
    onSecondary = FF.Ink,
    tertiary = FF.Crimson,
    onTertiary = FF.TextPrimary,
    error = FF.Crimson,
    onError = FF.TextPrimary,
    background = FF.Ink,
    onBackground = FF.TextPrimary,
    surface = FF.Surface1,
    onSurface = FF.TextPrimary,
    surfaceVariant = FF.Surface2,
    onSurfaceVariant = FF.TextSecondary,
    surfaceContainer = FF.Surface1,
    surfaceContainerHigh = FF.Surface2,
    surfaceContainerHighest = FF.Surface2,
    surfaceContainerLow = FF.Surface1,
    surfaceContainerLowest = FF.Ink,
    outline = FF.Border,
    outlineVariant = FF.Border,
)

private val FFShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp),
)

@Composable
fun FinanceFlowTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = FFColors, typography = FFTypography, shapes = FFShapes, content = content)
}
