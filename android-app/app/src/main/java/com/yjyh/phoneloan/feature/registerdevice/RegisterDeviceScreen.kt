package com.yjyh.phoneloan.feature.registerdevice

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.font.FontWeight
import com.yjyh.phoneloan.core.data.MockPhoneLoanRepository
import com.yjyh.phoneloan.core.design.AppCard
import com.yjyh.phoneloan.core.design.AppColors
import com.yjyh.phoneloan.core.design.InteractiveField
import com.yjyh.phoneloan.core.design.MutedText
import com.yjyh.phoneloan.core.design.Page
import com.yjyh.phoneloan.core.design.PrimaryButton

@Composable
fun RegisterDeviceScreen(contentPadding: PaddingValues, imei: String, onBack: () -> Unit) {
    var deviceName by remember { mutableStateOf("") }
    var saved by remember { mutableStateOf(false) }
    val displayImei = imei.ifBlank { "867450991234568" }

    Page(title = "注册手机", contentPadding = contentPadding, topLink = "‹ 扫码借", onTopLink = onBack) {
        MutedText("系统识别到该 IMEI 尚未进入仓库，请填写手机名称，完成建档后再确认借走。")
        AppCard {
            MutedText("完整 IMEI")
            Text(displayImei, fontWeight = FontWeight.Bold)
            MutedText("IMEI2：未识别，可后续补充")
        }
        AppCard {
            InteractiveField(
                label = "手机名称",
                value = deviceName,
                onValueChange = { deviceName = it },
                placeholder = "例如 小米14 白 / 测试机A"
            )
            MutedText("建档后：当前登录用户成为设备绑定 owner，并记录为当前持有人。")
        }
        if (!saved) {
            PrimaryButton("确认建档并借走", onClick = {
                val name = deviceName.ifBlank { "未命名手机" }
                MockPhoneLoanRepository.addDevice(name = name, imei = displayImei)
                saved = true
            })
        }
        if (saved) {
            AppCard {
                Text("成功反馈：手机已建档，已记录你为当前持有人。", color = AppColors.Success)
            }
            PrimaryButton("完成", onClick = onBack)
        }
    }
}
