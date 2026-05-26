package com.yjyh.phoneloan.feature.registerdevice

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.font.FontWeight
import com.yjyh.phoneloan.core.analytics.AnalyticsLogger
import com.yjyh.phoneloan.core.data.PhoneLoanData
import com.yjyh.phoneloan.core.design.AppCard
import com.yjyh.phoneloan.core.design.AppColors
import com.yjyh.phoneloan.core.design.InteractiveField
import com.yjyh.phoneloan.core.design.MutedText
import com.yjyh.phoneloan.core.design.Page
import com.yjyh.phoneloan.core.design.PrimaryButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun RegisterDeviceScreen(contentPadding: PaddingValues, imei: String, onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    var deviceName by remember { mutableStateOf("") }
    var saved by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
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
                onValueChange = {
                    deviceName = it
                    errorMessage = ""
                },
                placeholder = "例如 小米14 白 / 测试机A"
            )
            MutedText("建档后：当前登录用户成为设备绑定 owner，并记录为当前持有人。")
        }
        if (errorMessage.isNotEmpty()) {
            AppCard {
                Text(errorMessage, color = AppColors.Error, fontWeight = FontWeight.Bold)
                MutedText("请检查 IMEI 是否已建档，或稍后重试。")
            }
        }
        if (!saved) {
            PrimaryButton("确认建档并借走", onClick = {
                if (loading) return@PrimaryButton
                val name = deviceName.ifBlank { "未命名手机" }
                AnalyticsLogger.trackAction(
                    name = "register_device_submit",
                    screen = "register_device",
                    payload = mapOf("hasCustomName" to deviceName.isNotBlank(), "imei" to displayImei)
                )
                loading = true
                errorMessage = ""
                scope.launch {
                    val result = withContext(Dispatchers.IO) {
                        PhoneLoanData.repository.addDeviceResult(name = name, imei = displayImei)
                    }
                    loading = false
                    result
                        .onSuccess { saved = true }
                        .onFailure {
                            errorMessage = it.message ?: "建档失败，请稍后再试"
                            AnalyticsLogger.trackError("register_device_visible_error", screen = "register_device", throwable = it)
                        }
                }
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
