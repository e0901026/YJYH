package com.yjyh.phoneloan.core.design

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Smartphone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.yjyh.phoneloan.app.AppRoute

@Composable
fun PhoneLoanBackground(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Page)
    ) {
        content()
    }
}

@Composable
fun Page(
    title: String,
    contentPadding: PaddingValues,
    topLink: String? = null,
    onTopLink: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val isBackAction = topLink?.startsWith("‹") == true

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(horizontal = 18.dp, vertical = 22.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(if (isBackAction) 4.dp else 0.dp),
                modifier = Modifier.weight(1f)
            ) {
                if (isBackAction && onTopLink != null) {
                    IconButton(onClick = onTopLink, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "返回",
                            tint = AppColors.Primary
                        )
                    }
                }
                Text(title, style = AppTypography.headlineLarge, color = AppColors.Text)
            }
            if (!isBackAction && topLink != null && onTopLink != null) {
                IconButton(onClick = onTopLink, modifier = Modifier.size(40.dp)) {
                    if (topLink == "+") {
                        Icon(
                            imageVector = Icons.Outlined.Add,
                            contentDescription = "添加",
                            tint = AppColors.Primary
                        )
                    } else {
                        Text(
                            text = topLink,
                            color = AppColors.Primary,
                            style = AppTypography.bodySmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
        content()
    }
}

@Composable
fun AppCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = AppColors.Card,
        shape = RoundedCornerShape(8.dp),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.Line)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            content = content
        )
    }
}

@Composable
fun PrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary)
    ) {
        Text(text, color = Color.White, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun SecondaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.Primary),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = AppColors.Primary)
    ) {
        Text(text, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun Field(label: String, value: String = "", placeholder: String = "") {
    Text(label, style = AppTypography.bodySmall.copy(fontWeight = FontWeight.Bold), color = AppColors.Text)
    OutlinedTextField(
        value = value,
        onValueChange = {},
        placeholder = { Text(placeholder, color = AppColors.Muted) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(8.dp)
    )
}

@Composable
fun InteractiveField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    keyboardType: androidx.compose.ui.text.input.KeyboardType = androidx.compose.ui.text.input.KeyboardType.Text,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    Text(label, style = AppTypography.bodySmall.copy(fontWeight = FontWeight.Bold), color = AppColors.Text)
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = AppColors.Muted) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(8.dp),
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = visualTransformation
    )
}

@Composable
fun StatusPill(text: String, color: Color = AppColors.Primary) {
    Text(
        text = text,
        color = color,
        style = AppTypography.bodySmall.copy(fontWeight = FontWeight.Bold),
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.10f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    )
}

@Composable
fun SegmentedTabs(
    items: List<String>,
    selected: Int,
    modifier: Modifier = Modifier,
    onSelected: ((Int) -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, AppColors.Line, RoundedCornerShape(8.dp))
            .background(AppColors.Card)
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        items.forEachIndexed { index, label ->
            val active = index == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (active) AppColors.PrimarySoft else AppColors.Card)
                    .clickable(enabled = onSelected != null) { onSelected?.invoke(index) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    label,
                    color = if (active) AppColors.Primary else AppColors.Muted,
                    style = AppTypography.bodySmall.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

@Composable
fun PhoneLoanBottomBar(currentRoute: String?, onNavigate: (AppRoute) -> Unit) {
    val items = listOf(
        Triple(AppRoute.Home, Icons.Outlined.Home, "首页"),
        Triple(AppRoute.Devices, Icons.Outlined.Smartphone, "设备"),
        Triple(AppRoute.Profile, Icons.Outlined.Person, "我的")
    )
    NavigationBar(containerColor = AppColors.Card, tonalElevation = 0.dp) {
        items.forEach { (route, icon, label) ->
            NavigationBarItem(
                selected = currentRoute == route.value,
                onClick = { onNavigate(route) },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = if (currentRoute == route.value) AppColors.Primary else AppColors.Muted
                    )
                },
                label = { Text(label) }
            )
        }
    }
}

@Composable
fun TinyIconBox(text: String, color: Color = AppColors.Primary) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.12f)),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = color, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun MutedText(text: String) {
    Text(text, style = AppTypography.bodySmall, color = AppColors.Muted)
}

@Composable
fun SectionGap() {
    Spacer(modifier = Modifier.height(2.dp))
}
