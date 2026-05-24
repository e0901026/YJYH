package com.yjyh.phoneloan.feature.home

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
import com.yjyh.phoneloan.core.data.MockPhoneLoanRepository
import com.yjyh.phoneloan.core.design.AppCard
import com.yjyh.phoneloan.core.design.AppColors
import com.yjyh.phoneloan.core.design.MutedText
import com.yjyh.phoneloan.core.design.Page
import com.yjyh.phoneloan.core.design.PrimaryButton
import com.yjyh.phoneloan.core.design.SecondaryButton

@Composable
fun HomeScreen(
    contentPadding: PaddingValues,
    onScanBorrow: () -> Unit,
    onReturnLoan: () -> Unit,
    onDevices: () -> Unit
) {
    val user = MockPhoneLoanRepository.currentUser()
    val heldCount = MockPhoneLoanRepository.heldCount()
    val borrowedInCount = MockPhoneLoanRepository.borrowedInCount()
    val borrowedOutCount = MockPhoneLoanRepository.borrowedOutCount()
    val pendingCount = borrowedInCount + borrowedOutCount
    Page(title = "首页", contentPadding = contentPadding, topLink = "邀请码 ${user.inviteUsed}/10") {
        AppCard {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("手上持有", color = AppColors.Muted)
                Text("查看列表", color = AppColors.Primary, fontWeight = FontWeight.Bold)
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
            Text("待处理", fontWeight = FontWeight.Bold)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                MutedText("我借入 ${borrowedInCount} 台待还 · 我借出 ${borrowedOutCount} 台未归还")
                Text("${pendingCount} 条", color = AppColors.Primary, fontWeight = FontWeight.Bold)
            }
        }
        AppCard {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("申请邀请码", fontWeight = FontWeight.Bold)
                Text("已用 ${user.inviteUsed}/10", color = AppColors.Primary, fontWeight = FontWeight.Bold)
            }
        }
        AppCard {
            Text("最近动态", fontWeight = FontWeight.Bold)
            MutedText(MockPhoneLoanRepository.latestActivity)
        }
    }
}
