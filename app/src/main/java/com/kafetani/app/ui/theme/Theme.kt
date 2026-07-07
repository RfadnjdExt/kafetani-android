package com.kafetani.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ─── Palet warna Kafetani: coklat espresso + hijau sage (hasil tani) + emas gula aren ───
val CoffeeBean = Color(0xFF3B2A20)
val CoffeeBeanLight = Color(0xFF5D4636)
val Parchment = Color(0xFFF7F1E6)
val Sage = Color(0xFF6F8F5B)
val SageDark = Color(0xFF4E6B3E)
val Marigold = Color(0xFFE0A438)
val Charcoal = Color(0xFF2B2320)
val ErrorClay = Color(0xFFB3261E)
val SurfaceWhite = Color(0xFFFFFDF9)

private val KafetaniColorScheme = lightColorScheme(
    primary = CoffeeBean,
    onPrimary = Parchment,
    primaryContainer = CoffeeBeanLight,
    onPrimaryContainer = Parchment,
    secondary = Sage,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDCE8D2),
    onSecondaryContainer = SageDark,
    tertiary = Marigold,
    onTertiary = Charcoal,
    background = Parchment,
    onBackground = Charcoal,
    surface = SurfaceWhite,
    onSurface = Charcoal,
    surfaceVariant = Color(0xFFEDE4D3),
    onSurfaceVariant = Color(0xFF564A3D),
    error = ErrorClay,
    onError = Color.White,
    outline = Color(0xFFB8AA95)
)

// Roboto bawaan Android (tanpa file font kustom) — dibedakan lewat bobot & ukuran saja.
private val baseFontFamily = FontFamily.Default

val KafetaniTypography = Typography(
    headlineMedium = TextStyle(
        fontFamily = baseFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 34.sp,
        letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontFamily = baseFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    titleMedium = TextStyle(
        fontFamily = baseFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 17.sp,
        lineHeight = 24.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = baseFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = baseFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    labelLarge = TextStyle(
        fontFamily = baseFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    )
)

val KafetaniShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun KafetaniTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = KafetaniColorScheme,
        typography = KafetaniTypography,
        shapes = KafetaniShapes,
        content = content
    )
}
