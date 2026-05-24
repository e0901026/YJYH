package com.yjyh.phoneloan.feature.returnloan

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.yjyh.phoneloan.core.analytics.AnalyticsLogger
import com.yjyh.phoneloan.core.data.PhoneLoanData
import com.yjyh.phoneloan.core.design.AppCard
import com.yjyh.phoneloan.core.design.AppColors
import com.yjyh.phoneloan.core.design.AppTypography
import com.yjyh.phoneloan.core.design.MutedText
import com.yjyh.phoneloan.core.design.Page
import com.yjyh.phoneloan.core.design.SegmentedTabs
import com.yjyh.phoneloan.core.model.LoanRecord

@Composable
fun ReturnLoanScreen(contentPadding: PaddingValues, onBack: () -> Unit) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var returnedDeviceName by remember { mutableStateOf("") }
    var urgedDeviceName by remember { mutableStateOf("") }
    val repository = PhoneLoanData.repository
    val loans = repository.activeLoans()
    val borrowedOutCount = loans.count { it.statusText == "我借出去的" }
    val borrowedInCount = loans.count { it.statusText == "我借入的" }
    val visibleLoans = loans.filter {
        if (selectedTab == 0) it.statusText == "我借出去的" else it.statusText == "我借入的"
    }

    Page(title = "一键还", contentPadding = contentPadding, topLink = "‹ 首页", onTopLink = onBack) {
        SegmentedTabs(
            items = listOf("我借出去的 $borrowedOutCount", "我借入的 $borrowedInCount"),
            selected = selectedTab,
            onSelected = {
                selectedTab = it
                AnalyticsLogger.trackAction(
                    name = "return_tab_switch",
                    screen = "return_loan",
                    payload = mapOf("tab" to if (it == 0) "borrowed_out" else "borrowed_in")
                )
            }
        )
        if (returnedDeviceName.isNotEmpty()) {
            AppCard {
                Text("已归还：$returnedDeviceName", color = AppColors.Success, fontWeight = FontWeight.Bold)
                MutedText("设备状态和首页手上持有台数已同步更新。")
            }
        }
        if (urgedDeviceName.isNotEmpty()) {
            AppCard {
                Text("已催还：$urgedDeviceName", color = AppColors.Primary, fontWeight = FontWeight.Bold)
                MutedText("已向当前持有人发送催还消息，设备状态不变。")
            }
        }
        visibleLoans.forEach { loan ->
            AppCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LoanSummary(loan = loan, modifier = Modifier.weight(1f))
                    if (loan.statusText == "我借入的") {
                        ReturnActionButton(
                            text = "一键还",
                            onClick = {
                                urgedDeviceName = ""
                                returnedDeviceName = loan.device.name
                                AnalyticsLogger.trackAction(
                                    name = "return_click",
                                    screen = "return_loan",
                                    payload = mapOf("deviceId" to loan.device.id)
                                )
                                repository.returnLoan(loan.device.id)
                            }
                        )
                    } else {
                        ReturnActionButton(
                            text = "催还机",
                            onClick = {
                                returnedDeviceName = ""
                                urgedDeviceName = loan.device.name
                                AnalyticsLogger.trackAction(
                                    name = "urge_return_click",
                                    screen = "return_loan",
                                    payload = mapOf("deviceId" to loan.device.id, "holderId" to loan.counterpart.id)
                                )
                                repository.urgeReturn(loan.device.id)
                            }
                        )
                    }
                }
            }
        }
        if (visibleLoans.isEmpty()) {
            AppCard {
                MutedText("当前没有需要处理的借还记录。")
            }
        }
        AppCard {
            Text("操作说明", fontWeight = FontWeight.Bold)
            MutedText("我借出去的设备可催还机，只发送提醒消息；归还必须由当前持有人在「我借入的」中点击「一键还」。")
        }
    }
}

@Composable
private fun LoanSummary(loan: LoanRecord, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(loan.device.name, fontWeight = FontWeight.Bold)
        if (loan.statusText == "我借入的") {
            MutedText("对方：${loan.counterpart.name} · 工号 ${loan.counterpart.employeeNo}")
        } else {
            MutedText("当前持有人：${loan.counterpart.name} · 工号 ${loan.counterpart.employeeNo}")
        }
        MutedText("IMEI：${loan.device.imei1}")
        if (loan.statusText == "我借出去的") {
            MutedText("持有天数：${holdDaysText(loan)}")
        }
    }
}

@Composable
private fun ReturnActionButton(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .width(82.dp)
            .height(40.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(AppColors.Primary)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            style = AppTypography.bodySmall.copy(fontWeight = FontWeight.Bold),
            maxLines = 1,
            overflow = TextOverflow.Clip
        )
    }
}

private fun holdDaysText(loan: LoanRecord): String {
    return when (loan.device.id) {
        "d1" -> "6 天"
        else -> loan.startedAt
    }
}
