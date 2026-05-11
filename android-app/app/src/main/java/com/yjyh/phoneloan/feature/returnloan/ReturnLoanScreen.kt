package com.yjyh.phoneloan.feature.returnloan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.yjyh.phoneloan.core.data.MockPhoneLoanRepository
import com.yjyh.phoneloan.core.design.AppCard
import com.yjyh.phoneloan.core.design.AppColors
import com.yjyh.phoneloan.core.design.MutedText
import com.yjyh.phoneloan.core.design.Page
import com.yjyh.phoneloan.core.design.PrimaryButton
import com.yjyh.phoneloan.core.design.SegmentedTabs

@Composable
fun ReturnLoanScreen(contentPadding: PaddingValues, onBack: () -> Unit) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var returnedDeviceName by remember { mutableStateOf("") }
    val loans = MockPhoneLoanRepository.activeLoans()
    val borrowedOutCount = loans.count { it.statusText == "我借出去的" }
    val borrowedInCount = loans.count { it.statusText == "我借入的" }
    val visibleLoans = loans.filter {
        if (selectedTab == 0) it.statusText == "我借出去的" else it.statusText == "我借入的"
    }

    Page(title = "一键还", contentPadding = contentPadding, topLink = "‹ 首页", onTopLink = onBack) {
        SegmentedTabs(
            items = listOf("我借出去的 $borrowedOutCount", "我借入的 $borrowedInCount"),
            selected = selectedTab,
            onSelected = { selectedTab = it }
        )
        if (returnedDeviceName.isNotEmpty()) {
            AppCard {
                Text("已归还：$returnedDeviceName", color = AppColors.Success, fontWeight = FontWeight.Bold)
                MutedText("设备状态和首页手上持有台数已同步更新。")
            }
        }
        visibleLoans.forEach { loan ->
            AppCard {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(loan.device.name, fontWeight = FontWeight.Bold)
                    Text(loan.statusText, color = AppColors.Primary, fontWeight = FontWeight.Bold)
                }
                MutedText("对方：${loan.counterpart.name} · ${loan.counterpart.employeeNo}")
                PrimaryButton(
                    text = "一键还",
                    onClick = {
                        returnedDeviceName = loan.device.name
                        MockPhoneLoanRepository.returnLoan(loan.device.id)
                    }
                )
            }
        }
        if (visibleLoans.isEmpty()) {
            AppCard {
                MutedText("当前没有需要处理的借还记录。")
            }
        }
        AppCard {
            Text("二次确认示例", fontWeight = FontWeight.Bold)
            MutedText("确认后将结束当前借用记录，并更新手上持有台数。")
        }
    }
}
