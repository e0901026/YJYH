package com.yjyh.phoneloan.feature.profile

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import com.yjyh.phoneloan.core.data.MockPhoneLoanRepository
import com.yjyh.phoneloan.core.design.AppCard
import com.yjyh.phoneloan.core.design.AppColors
import com.yjyh.phoneloan.core.design.MutedText
import com.yjyh.phoneloan.core.design.Page
import com.yjyh.phoneloan.core.design.PrimaryButton
import com.yjyh.phoneloan.core.design.SecondaryButton

@Composable
fun ProfileScreen(contentPadding: PaddingValues, onOwnerUsers: () -> Unit) {
    val user = MockPhoneLoanRepository.currentUser()
    Page(title = "我的", contentPadding = contentPadding, topLink = "Owner") {
        AppCard {
            Text(user.name, fontWeight = FontWeight.Bold)
            MutedText("工号 ${user.employeeNo}")
        }
        AppCard {
            Text("邀请码", fontWeight = FontWeight.Bold)
            MutedText("普通用户终身累计最多申请 10 张，已使用、过期、作废均计入配额。")
            Text("已用 ${user.inviteUsed}/10", color = AppColors.Primary, fontWeight = FontWeight.Bold)
            PrimaryButton("申请邀请码", onOwnerUsers)
            Text("配额已满提示：邀请码配额已用完（10/10）", color = AppColors.Error)
        }
        SecondaryButton("修改密码", onOwnerUsers)
        SecondaryButton("管理后台", onOwnerUsers)
        SecondaryButton("退出登录", onOwnerUsers)
    }
}
