package com.example.ui.theme

import android.os.Build
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = BentoDarkPrimary,
    onPrimary = BentoDarkOnPrimary,
    primaryContainer = BentoDarkPrimaryContainer,
    onPrimaryContainer = BentoDarkOnPrimaryContainer,
    secondary = BentoDarkSecondary,
    onSecondary = BentoDarkOnSecondary,
    secondaryContainer = BentoDarkSecondaryContainer,
    onSecondaryContainer = BentoDarkOnSecondaryContainer,
    tertiary = BentoDarkTertiary,
    background = BentoDarkBackground,
    onBackground = BentoDarkOnBackground,
    surface = BentoDarkSurface,
    onSurface = BentoDarkOnSurface,
    surfaceVariant = BentoDarkSurfaceVariant,
    onSurfaceVariant = BentoDarkOnSurfaceVariant,
    outline = BentoDarkOutline,
    outlineVariant = BentoDarkOutlineVariant,
    error = BentoDarkError,
    errorContainer = BentoDarkErrorContainer,
    onError = Color(0xFF601410),
    onErrorContainer = BentoDarkOnErrorContainer
  )

private val LightColorScheme =
  lightColorScheme(
    primary = BentoPrimary,
    onPrimary = BentoOnPrimary,
    primaryContainer = BentoPrimaryContainer,
    onPrimaryContainer = BentoOnPrimaryContainer,
    secondary = BentoSecondary,
    onSecondary = BentoOnSecondary,
    secondaryContainer = BentoSecondaryContainer,
    onSecondaryContainer = BentoOnSecondaryContainer,
    tertiary = BentoTertiary,
    background = BentoBackground,
    onBackground = BentoOnBackground,
    surface = BentoSurface,
    onSurface = BentoOnSurface,
    surfaceVariant = BentoSurfaceVariant,
    onSurfaceVariant = BentoOnSurfaceVariant,
    outline = BentoOutline,
    outlineVariant = BentoOutlineVariant,
    error = BentoError,
    errorContainer = BentoErrorContainer,
    onError = Color.White,
    onErrorContainer = BentoOnErrorContainer
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Disabling dynamic colors by default so Bento aesthetic is always active
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
