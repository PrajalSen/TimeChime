package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

import com.example.domain.model.AppThemeMode

private val LightColorScheme = lightColorScheme(
    primary = PrimaryIndigo,
    onPrimary = LightOnPrimary,
    primaryContainer = Color(0xFFEEF2FF),
    onPrimaryContainer = Color(0xFF3730A3),
    secondary = SecondaryCyan,
    onSecondary = LightOnPrimary,
    secondaryContainer = Color(0xFFCFFAFE),
    onSecondaryContainer = Color(0xFF155E75),
    background = Color(0xFFF8FAFC),
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = LightOnSurfaceVariant,
    outlineVariant = Color(0xFFE2E8F0),
    error = ErrorRed
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryIndigo,
    onPrimary = DarkOnPrimary,
    primaryContainer = PrimaryIndigo.copy(alpha = 0.2f),
    secondary = SecondaryCyan,
    onSecondary = DarkOnPrimary,
    secondaryContainer = SecondaryCyan.copy(alpha = 0.2f),
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    error = ErrorRed
)

private val AuroraDarkColorScheme = darkColorScheme(
    primary = Color(0xFFA855F7), // Purple accent
    onPrimary = Color.White,
    primaryContainer = Color(0xFF3B0764),
    onPrimaryContainer = Color(0xFFE9D5FF),
    secondary = Color(0xFF38BDF8), // Subtle blue highlight
    onSecondary = Color(0xFF0F172A),
    secondaryContainer = Color(0xFF075985),
    onSecondaryContainer = Color(0xFFBAE6FD),
    background = Color(0xFF12131C), // Deep Charcoal
    onBackground = Color(0xFFF1F5F9),
    surface = Color(0xFF181A26), // Dark gradient surface
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF1F2232),
    onSurfaceVariant = Color(0xFF94A3B8),
    outlineVariant = Color(0xFF2E334D),
    error = ErrorRed
)

private val AuroraBloomColorScheme = lightColorScheme(
    primary = Color(0xFFD946EF), // Soft magenta
    onPrimary = Color.White,
    primaryContainer = Color(0xFFF3E8FF), // Lavender container
    onPrimaryContainer = Color(0xFF701A75),
    secondary = Color(0xFF06B6D4), // Cool cyan
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCFFAFE), // Light cyan container
    onSecondaryContainer = Color(0xFF155E75),
    background = Color(0xFFFAF8FF), // Cool subtle lavender white background
    onBackground = Color(0xFF1E1B2E),
    surface = Color.White,
    onSurface = Color(0xFF1E1B2E),
    surfaceVariant = Color(0xFFF3E8FF), // Lavender surface variant
    onSurfaceVariant = Color(0xFF581C87),
    outlineVariant = Color(0xFFDDD6FE),
    error = ErrorRed
)

private val RoseMistColorScheme = lightColorScheme(
    primary = Color(0xFFE11D48), // Muted rose
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFF1F2), // Soft blush
    onPrimaryContainer = Color(0xFF881337),
    secondary = Color(0xFFD97706), // Warm champagne amber accent
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFEF3C7),
    onSecondaryContainer = Color(0xFF78350F),
    background = Color(0xFFFFFDF7), // Warm ivory background
    onBackground = Color(0xFF292524),
    surface = Color(0xFFFFFBF5), // Warm ivory surface
    onSurface = Color(0xFF292524),
    surfaceVariant = Color(0xFFFAF5EF), // Very light champagne surface variant
    onSurfaceVariant = Color(0xFF57534E),
    outlineVariant = Color(0xFFF5E0D8),
    error = ErrorRed
)

private val MidnightIndigoColorScheme = darkColorScheme(
    primary = Color(0xFF38BDF8), // Soft sky blue accent
    onPrimary = Color(0xFF0C4A6E),
    primaryContainer = Color(0xFF1E293B), // Elevated surface
    onPrimaryContainer = Color(0xFFE0F2FE),
    secondary = Color(0xFF818CF8), // Indigo secondary
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF312E81),
    onSecondaryContainer = Color(0xFFE0E7FF),
    background = Color(0xFF0F172A), // Dark navy background
    onBackground = Color(0xFFF8FAFC),
    surface = Color(0xFF1E293B), // Gentle elevated surface
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF334155),
    onSurfaceVariant = Color(0xFFCBD5E1),
    outlineVariant = Color(0xFF475569),
    error = ErrorRed
)

@Composable
fun TimeChimeTheme(
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when (themeMode) {
        AppThemeMode.AURORA_DARK -> AuroraDarkColorScheme
        AppThemeMode.AURORA_BLOOM -> AuroraBloomColorScheme
        AppThemeMode.ROSE_MIST -> RoseMistColorScheme
        AppThemeMode.MIDNIGHT_INDIGO -> MidnightIndigoColorScheme
        AppThemeMode.LIGHT -> LightColorScheme
        AppThemeMode.DARK -> DarkColorScheme
        AppThemeMode.SYSTEM -> {
            if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val context = LocalContext.current
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            } else if (darkTheme) DarkColorScheme else LightColorScheme
        }
    }

    val isDarkBar = when (themeMode) {
        AppThemeMode.AURORA_DARK, AppThemeMode.MIDNIGHT_INDIGO, AppThemeMode.DARK -> true
        AppThemeMode.AURORA_BLOOM, AppThemeMode.ROSE_MIST, AppThemeMode.LIGHT -> false
        AppThemeMode.SYSTEM -> darkTheme
    }


    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                window.navigationBarColor = colorScheme.background.toArgb()
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !isDarkBar
                insetsController.isAppearanceLightNavigationBars = !isDarkBar
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        shapes = Shapes,
        typography = Typography,
        content = content
    )
}
