package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = LightLeafGreen,
    secondary = HarvestGoldSecondary,
    tertiary = EarthSiennaTertiary,
    background = DeepJungleBg,
    surface = JungleSurface,
    onPrimary = OrganicLightCreamText,
    onSecondary = CharcoalText,
    onBackground = OrganicLightCreamText,
    onSurface = OrganicLightCreamText,
    primaryContainer = ForestGreenPrimary,
    onPrimaryContainer = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = ForestGreenPrimary,
    secondary = HarvestGoldSecondary,
    tertiary = EarthSiennaTertiary,
    background = OrganicCreamBg,
    surface = EarthSurface,
    onPrimary = Color.White,
    onSecondary = CharcoalText,
    onBackground = CharcoalText,
    onSurface = CharcoalText,
    primaryContainer = LightLeafGreen,
    onPrimaryContainer = Color.White
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is disabled by default to keep the High Density theme design
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
