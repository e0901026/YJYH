package com.yjyh.phoneloan.feature.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yjyh.phoneloan.core.analytics.AnalyticsLogger
import com.yjyh.phoneloan.core.data.PhoneLoanData
import com.yjyh.phoneloan.core.design.AppCard
import com.yjyh.phoneloan.core.design.AppColors
import com.yjyh.phoneloan.core.design.MutedText
import com.yjyh.phoneloan.core.design.Page
import com.yjyh.phoneloan.core.design.PrimaryButton
import com.yjyh.phoneloan.core.design.SecondaryButton
import com.yjyh.phoneloan.core.design.StatusPill
import com.yjyh.phoneloan.core.model.DeviceStatus

@Composable
fun HomeScreen(
    contentPadding: PaddingValues,
    onScanBorrow: () -> Unit,
    onReturnLoan: () -> Unit,
    onDevices: () -> Unit
) {
    val repository = PhoneLoanData.repository
    val user = repository.currentUser()
    val heldCount = repository.heldCount()
    val latestDevices = repository.devices().sortedByDescending { it.latestEventOrder }
    Page(title = "首页", contentPadding = contentPadding, topLink = "邀请码 ${user.inviteUsed}/10") {
        AppCard {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("手上持有", color = AppColors.Muted)
                Text(
                    "查看列表",
                    color = AppColors.Primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable {
                        AnalyticsLogger.trackAction("home_view_on_hand_devices_click", screen = "home")
                        onDevices()
                    }
                )
            }
            Text("${heldCount} 台", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = AppColors.Text)
            MutedText("含自有在手设备与借入待还设备，系统自动计算。")
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PrimaryButton("扫码借", {
                AnalyticsLogger.trackAction("scan_borrow_entry_click", screen = "home")
                onScanBorrow()
            }, Modifier.weight(1f))
            SecondaryButton("一键还", {
                AnalyticsLogger.trackAction("return_loan_entry_click", screen = "home")
                onReturnLoan()
            }, Modifier.weight(1f))
        }
        AppCard {
            Text("最新动态", fontWeight = FontWeight.Bold)
            latestDevices.forEach { device ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(device.name, fontWeight = FontWeight.Bold)
                    StatusPill(
                        text = when (device.status) {
                            DeviceStatus.HELD_BY_ME -> "在我手上"
                            DeviceStatus.BORROWED_OUT -> "已借出"
                            DeviceStatus.PENDING_RETURN -> "借入待还"
                            DeviceStatus.AVAILABLE -> "可借"
                        },
                        color = when (device.status) {
                            DeviceStatus.HELD_BY_ME -> AppColors.Success
                            DeviceStatus.BORROWED_OUT -> AppColors.Warning
                            DeviceStatus.PENDING_RETURN -> AppColors.Primary
                            DeviceStatus.AVAILABLE -> AppColors.Muted
                        }
                    )
                }
                MutedText("当前持有人：${device.currentHolder?.name ?: "暂无"} · ${device.currentHolder?.employeeNo ?: "--"}")
                MutedText(device.latestEventLabel.ifBlank { "最新状态：后端记录" })
            }
        }
    }
}
