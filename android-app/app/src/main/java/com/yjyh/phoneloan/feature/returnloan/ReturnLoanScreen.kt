package com.yjyh.phoneloan.feature.returnloan

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
import com.yjyh.phoneloan.core.design.MutedText
import com.yjyh.phoneloan.core.design.Page
import com.yjyh.phoneloan.core.design.PrimaryButton
import com.yjyh.phoneloan.core.design.SegmentedTabs

@Composable
fun ReturnLoanScreen(contentPadding: PaddingValues, onBack: () -> Unit) {
    Page(title = "一键还", contentPadding = contentPadding, topLink = "‹ 首页", onTopLink = onBack) {
        SegmentedTabs(listOf("我借出去的 4", "我借入的 3"), selected = 0)
        MockPhoneLoanRepository.activeLoans().forEach { loan ->
            AppCard {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(loan.device.name, fontWeight = FontWeight.Bold)
                    Text(loan.statusText, color = AppColors.Primary, fontWeight = FontWeight.Bold)
                }
                MutedText("对方：${loan.counterpart.name} · ${loan.counterpart.employeeNo}")
                PrimaryButton("一键还", onBack)
            }
        }
        AppCard {
            Text("二次确认示例", fontWeight = FontWeight.Bold)
            MutedText("确认后将结束当前借用记录，并更新手上持有台数。")
        }
    }
}
