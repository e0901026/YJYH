package com.yjyh.phoneloan.feature.scanborrow

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.yjyh.phoneloan.core.data.MockPhoneLoanRepository
import com.yjyh.phoneloan.core.design.AppCard
import com.yjyh.phoneloan.core.design.AppColors
import com.yjyh.phoneloan.core.design.InteractiveField
import com.yjyh.phoneloan.core.design.MutedText
import com.yjyh.phoneloan.core.design.Page
import com.yjyh.phoneloan.core.design.PrimaryButton
import com.yjyh.phoneloan.core.design.SecondaryButton
import com.yjyh.phoneloan.core.model.DeviceStatus
import com.yjyh.phoneloan.core.model.UserSummary

/** IMEI format: 15 digits only */
private val IMEI_PATTERN = Regex("^\\d{15}$")

@Composable
fun ScanBorrowScreen(
    contentPadding: PaddingValues,
    onBack: () -> Unit,
    onRegisterDevice: (imei: String) -> Unit
) {
    val user = MockPhoneLoanRepository.currentUser()
    val meSummary = remember { UserSummary(user.id, user.employeeNo, user.name) }

    var imeiInput by remember { mutableStateOf("") }
    var scannedImei by remember { mutableStateOf("") }
    var scanState by remember { mutableStateOf(ScanState.IDLE) }
    var foundDeviceName by remember { mutableStateOf("") }
    var foundDeviceId by remember { mutableStateOf("") }
    var foundOwnerName by remember { mutableStateOf("") }
    var foundOwnerNo by remember { mutableStateOf("") }
    var foundHolderName by remember { mutableStateOf("") }
    var foundHolderNo by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var borrowSuccess by remember { mutableStateOf(false) }

    fun resetScan() {
        imeiInput = ""
        scannedImei = ""
        scanState = ScanState.IDLE
        foundDeviceName = ""
        foundDeviceId = ""
        foundOwnerName = ""
        foundOwnerNo = ""
        foundHolderName = ""
        foundHolderNo = ""
        errorMessage = ""
        borrowSuccess = false
    }

    Page(title = "扫码识别手机", contentPadding = contentPadding, topLink = "‹ 返回", onTopLink = onBack) {
        // Camera placeholder
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .background(Color(0xFF0F172A), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("⌗", color = Color.White, fontWeight = FontWeight.Bold)
        }
        MutedText("点击首页扫码借后调起相机，扫描手机 IMEI 条码。当前为模拟模式，请手动输入 15 位 IMEI 编码。")

        // IMEI input
        InteractiveField(
            label = "IMEI 编码",
            value = imeiInput,
            onValueChange = { imeiInput = it },
            placeholder = "输入 15 位 IMEI 编号，如 869301065812347",
            keyboardType = KeyboardType.Number
        )

        if (errorMessage.isNotEmpty()) {
            Text(errorMessage, color = AppColors.Error, fontWeight = FontWeight.Bold)
        }

        PrimaryButton("识别 IMEI") {
            val trimmed = imeiInput.trim()
            if (!IMEI_PATTERN.matches(trimmed)) {
                errorMessage = "请输入 15 位纯数字 IMEI 编码"
                scanState = ScanState.IDLE
                return@PrimaryButton
            }
            errorMessage = ""
            scannedImei = trimmed
            borrowSuccess = false

            val device = MockPhoneLoanRepository.findDeviceByImei(trimmed)
            if (device != null) {
                scanState = ScanState.FOUND
                foundDeviceName = device.name
                foundDeviceId = device.id
                foundOwnerName = device.owner.name
                foundOwnerNo = device.owner.employeeNo
                foundHolderName = device.currentHolder?.name ?: "暂无"
                foundHolderNo = device.currentHolder?.employeeNo ?: ""
            } else {
                scanState = ScanState.NOT_FOUND
            }
        }

        Spacer(Modifier.height(12.dp))

        when (scanState) {
            ScanState.FOUND -> {
                AppCard {
                    MutedText("识别到完整 IMEI")
                    Text(scannedImei, fontWeight = FontWeight.Bold, color = AppColors.Text)
                    MutedText("被借手机")
                    Text("$foundDeviceName · 已建档", fontWeight = FontWeight.Bold)
                }
                AppCard {
                    Text("当前记录", fontWeight = FontWeight.Bold)
                    MutedText("上一位持有人：$foundHolderName · $foundHolderNo")
                    MutedText("绑定 owner：$foundOwnerName · $foundOwnerNo")
                    MutedText("状态：可被借走，确认后系统自动更新持有人")
                }

                if (!borrowSuccess) {
                    PrimaryButton("确认借走") {
                        MockPhoneLoanRepository.updateDeviceHolder(
                            deviceId = foundDeviceId,
                            newHolder = meSummary,
                            newStatus = DeviceStatus.HELD_BY_ME
                        )
                        borrowSuccess = true
                    }
                }

                if (borrowSuccess) {
                    AppCard {
                        Text(
                            "成功：已记录你为最新持有人，并通知上一位持有人和绑定 owner。",
                            color = AppColors.Success
                        )
                    }
                }

                SecondaryButton("重新扫描") { resetScan() }
            }

            ScanState.NOT_FOUND -> {
                AppCard {
                    MutedText("完整 IMEI")
                    Text(scannedImei, fontWeight = FontWeight.Bold)
                    MutedText("该 IMEI 尚未建档，请先注册设备。")
                }
                PrimaryButton("注册新设备") {
                    onRegisterDevice(scannedImei)
                }
                SecondaryButton("重新输入") { resetScan() }
            }

            ScanState.IDLE -> { /* initial state, no result shown */ }
        }
    }
}

private enum class ScanState { IDLE, FOUND, NOT_FOUND }
