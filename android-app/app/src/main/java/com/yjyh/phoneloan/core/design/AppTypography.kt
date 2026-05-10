package com.yjyh.phoneloan.core.design

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val AppTypography = Typography(
    headlineLarge = Typography().headlineLarge.copy(
        fontSize = 26.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.sp
    ),
    titleMedium = Typography().titleMedium.copy(
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.sp
    ),
    bodyMedium = Typography().bodyMedium.copy(
        fontSize = 14.sp,
        letterSpacing = 0.sp
    ),
    bodySmall = Typography().bodySmall.copy(
        fontSize = 12.sp,
        letterSpacing = 0.sp
    ),
    labelLarge = Typography().labelLarge.copy(
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.sp
    )
)
