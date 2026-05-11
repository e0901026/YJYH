package com.yjyh.phoneloan.feature.devices

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.yjyh.phoneloan.core.data.MockPhoneLoanRepository
import com.yjyh.phoneloan.core.design.AppCard
import com.yjyh.phoneloan.core.design.AppColors
import com.yjyh.phoneloan.core.design.Field
import com.yjyh.phoneloan.core.design.MutedText
import com.yjyh.phoneloan.core.design.Page
import com.yjyh.phoneloan.core.design.PrimaryButton
import com.yjyh.phoneloan.core.design.SecondaryButton
import com.yjyh.phoneloan.core.design.SegmentedTabs
import com.yjyh.phoneloan.core.design.StatusPill
import com.yjyh.phoneloan.core.model.DeviceStatus

@Composable
fun DevicesScreen(contentPadding: PaddingValues, onAddDevice: () -> Unit, onOpenDevice: (String) -> Unit) {
    Page(title = "设备列表", contentPadding = contentPadding, topLink = "+", onTopLink = onAddDevice) {
        SegmentedTabs(listOf("全部", "在我手上", "已借出", "借入待还"), selected = 0)
        Field(label = "", placeholder = "搜索设备名或 IMEI")
        MockPhoneLoanRepository.devices().forEach { device ->
            AppCard(modifier = Modifier.clickable { onOpenDevice(device.id) }) {
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
                MutedText("IMEI ${device.imei1} · ${device.currentHolder?.name ?: "暂无持有人"}")
            }
        }
        PrimaryButton("添加设备（扫 IMEI）", onAddDevice)
    }
}

@Composable
fun DeviceDetailScreen(contentPadding: PaddingValues, onBack: () -> Unit) {
    val device = MockPhoneLoanRepository.devices()[1]
    Page(title = "设备详情", contentPadding = contentPadding, topLink = "‹ 设备", onTopLink = onBack) {
        AppCard {
            Text(device.name, fontWeight = FontWeight.Bold)
            MutedText("绑定 owner：${device.owner.name} · ${device.owner.employeeNo}")
            SecondaryButton("编辑设备名称（仅 owner）", onBack)
        }
        AppCard {
            Text("硬件标识", fontWeight = FontWeight.Bold)
            MutedText("IMEI：${device.imei1}")
            MutedText("IMEI2：${device.imei2 ?: "未录入"}")
            MutedText("完整 IMEI 仅 owner 或审计导出可见。")
        }
        AppCard {
            Text("当前状态", fontWeight = FontWeight.Bold)
            MutedText("当前持有人：${device.currentHolder?.name ?: "暂无"}")
            MutedText("自有且在本人手上，手上持有台数会自动计入。")
        }
        SecondaryButton("转让 owner", onBack)
        SecondaryButton("查看借用流水", onBack)
        AppCard {
            Text("边界示例：非 owner 隐藏编辑入口，或提示无权限。", color = AppColors.Error)
        }
    }
}
