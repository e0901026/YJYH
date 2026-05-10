package com.yjyh.phoneloan.feature.scanborrow

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yjyh.phoneloan.core.design.AppCard
import com.yjyh.phoneloan.core.design.AppColors
import com.yjyh.phoneloan.core.design.MutedText
import com.yjyh.phoneloan.core.design.Page
import com.yjyh.phoneloan.core.design.PrimaryButton
import com.yjyh.phoneloan.core.design.SecondaryButton

@Composable
fun ScanBorrowScreen(contentPadding: PaddingValues, onBack: () -> Unit, onUnknownDevice: () -> Unit) {
    Page(title = "扫码识别手机", contentPadding = contentPadding, topLink = "‹ 返回", onTopLink = onBack) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .background(Color(0xFF0F172A), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("⌗", color = Color.White, fontWeight = FontWeight.Bold)
        }
        MutedText("点击首页扫码借后调起相机，扫描手机 IMEI 条码。")
        AppCard {
            MutedText("识别到完整 IMEI")
            Text("869301065812347", fontWeight = FontWeight.Bold, color = AppColors.Text)
            MutedText("被借手机")
            Text("小米14 白 · 已建档", fontWeight = FontWeight.Bold)
        }
        AppCard {
            Text("当前记录", fontWeight = FontWeight.Bold)
            MutedText("上一位持有人：李雷 · 10248")
            MutedText("绑定 owner：王晓明 · 10086")
            MutedText("状态：可被借走，确认后系统自动更新持有人")
        }
        PrimaryButton("确认借走", onBack)
        AppCard {
            Text("成功：已记录你为最新持有人，并通知上一位持有人和绑定 owner。", color = AppColors.Success)
        }
        SecondaryButton("模拟未建档手机", onUnknownDevice)
    }
}
