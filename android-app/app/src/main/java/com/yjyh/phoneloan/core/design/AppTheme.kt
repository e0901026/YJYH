package com.yjyh.phoneloan.core.design

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = AppColors.Primary,
    background = AppColors.Page,
    surface = AppColors.Card,
    onPrimary = AppColors.Card,
    onBackground = AppColors.Text,
    onSurface = AppColors.Text,
    outline = AppColors.Line
)

@Composable
fun PhoneLoanTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = AppTypography,
        content = content
    )
}
