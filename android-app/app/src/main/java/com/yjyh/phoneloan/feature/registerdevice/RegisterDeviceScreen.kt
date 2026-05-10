package com.yjyh.phoneloan.feature.registerdevice

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import com.yjyh.phoneloan.core.design.AppCard
import com.yjyh.phoneloan.core.design.AppColors
import com.yjyh.phoneloan.core.design.Field
import com.yjyh.phoneloan.core.design.MutedText
import com.yjyh.phoneloan.core.design.Page
import com.yjyh.phoneloan.core.design.PrimaryButton

@Composable
fun RegisterDeviceScreen(contentPadding: PaddingValues, onBack: () -> Unit) {
    Page(title = "注册手机", contentPadding = contentPadding, topLink = "‹ 扫码借", onTopLink = onBack) {
        MutedText("系统识别到该 IMEI 尚未进入仓库，请填写手机名称，完成建档后再确认借走。")
        AppCard {
            MutedText("完整 IMEI")
            Text("867450991234568", fontWeight = FontWeight.Bold)
            MutedText("IMEI2：未识别，可后续补充")
        }
        AppCard {
            Field(label = "手机名称", placeholder = "例如 小米14 白 / 测试机A")
            MutedText("建档后：当前登录用户成为设备绑定 owner，并记录为当前持有人。")
        }
        PrimaryButton("确认建档并借走", onBack)
        AppCard {
            Text("成功反馈：手机已建档，已记录你为当前持有人。", color = AppColors.Success)
        }
    }
}
