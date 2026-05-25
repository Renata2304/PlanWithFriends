package com.example.planwithfriends.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import java.time.LocalDate

// --- PRIMĂVARĂ ---
private val SpringColorScheme = lightColorScheme(
    primary = SpringPrimary,
    secondary = SpringSecondary,
    tertiary = SpringTertiary,
    background = SpringBackground,
    surface = Color.White,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.Black
)

private val SpringDarkColorScheme = darkColorScheme(
    primary = SpringPrimary,
    secondary = SpringSecondary,
    tertiary = SpringTertiary,
    background = AppBackgroundDark,
    surface = AppSurfaceDark,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.Black
)

// --- VARĂ ---
private val SummerColorScheme = lightColorScheme(
    primary = SummerPrimary,
    secondary = SummerSecondary,
    tertiary = SummerTertiary,
    background = SummerBackground,
    surface = Color.White,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.Black
)

private val SummerDarkColorScheme = darkColorScheme(
    primary = SummerPrimary,
    secondary = SummerSecondaryDark,
    tertiary = SummerTertiaryDark,
    background = AppBackgroundDark,
    surface = AppSurfaceDark,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.Black
)

// --- TOAMNĂ ---
private val AutumnColorScheme = lightColorScheme(
    primary = AutumnPrimary,
    secondary = AutumnSecondary,
    tertiary = AutumnTertiary,
    background = AutumnBackground,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.Black
)

private val AutumnDarkColorScheme = darkColorScheme(
    primary = AutumnPrimary,
    secondary = AutumnSecondary,
    tertiary = AutumnTertiary,
    background = AppBackgroundDark,
    surface = AppSurfaceDark,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.Black
)

// --- IARNĂ ---
private val WinterColorScheme = lightColorScheme(
    primary = WinterPrimary,
    secondary = WinterSecondary,
    tertiary = WinterTertiary,
    background = WinterBackground,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White
)

private val WinterDarkColorScheme = darkColorScheme(
    primary = WinterPrimary,
    secondary = WinterSecondary,
    tertiary = WinterTertiary,
    background = AppBackgroundDark,
    surface = AppSurfaceDark,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White
)

@Composable
fun PlanWithFriendsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    season: String = "Auto (Anotimpuri)",
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val actualSeason = if (season == "Auto (Anotimpuri)") {
        when (LocalDate.now().monthValue) {
            in 3..5 -> "Primăvară"
            in 6..8 -> "Vară"
            in 9..11 -> "Toamnă"
            else -> "Iarnă"
        }
    } else {
        season
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> {
            when (actualSeason) {
                "Primăvară" -> SpringDarkColorScheme
                "Vară" -> SummerDarkColorScheme
                "Toamnă" -> AutumnDarkColorScheme
                "Iarnă" -> WinterDarkColorScheme
                else -> SpringDarkColorScheme
            }
        }
        else -> {
            when (actualSeason) {
                "Primăvară" -> SpringColorScheme
                "Vară" -> SummerColorScheme
                "Toamnă" -> AutumnColorScheme
                "Iarnă" -> WinterColorScheme
                else -> SpringColorScheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}