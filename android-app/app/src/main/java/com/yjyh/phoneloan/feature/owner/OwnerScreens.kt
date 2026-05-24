package com.yjyh.phoneloan.feature.owner

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.yjyh.phoneloan.core.data.PhoneLoanData
import com.yjyh.phoneloan.core.design.AppCard
import com.yjyh.phoneloan.core.design.AppColors
import com.yjyh.phoneloan.core.design.Field
import com.yjyh.phoneloan.core.design.MutedText
import com.yjyh.phoneloan.core.design.Page
import com.yjyh.phoneloan.core.design.PrimaryButton
import com.yjyh.phoneloan.core.design.SegmentedTabs
import com.yjyh.phoneloan.core.design.StatusPill
import com.yjyh.phoneloan.core.model.InviteStatus

@Composable
fun OwnerUsersScreen(contentPadding: PaddingValues, onBack: () -> Unit, onInvites: () -> Unit) {
    Page(title = "用户管理", contentPadding = contentPadding, topLink = "‹ 我的", onTopLink = onBack) {
        SegmentedTabs(listOf("用户列表", "邀请码"), selected = 0)
        Field(label = "", placeholder = "搜索工号 / 名称")
        AppCard {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("工号/名称", color = AppColors.Primary, fontWeight = FontWeight.Bold)
                Text("注册时间", color = AppColors.Primary, fontWeight = FontWeight.Bold)
                Text("邀请人", color = AppColors.Primary, fontWeight = FontWeight.Bold)
            }
            PhoneLoanData.repository.ownerUsers().forEach { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    MutedText("${row.employeeNo}\n${row.name}")
                    MutedText(row.registeredAt)
                    MutedText(row.inviter)
                }
            }
        }
        PrimaryButton("查看邀请码", onInvites)
    }
}

@Composable
fun OwnerInvitesScreen(contentPadding: PaddingValues, onBack: () -> Unit, onUsers: () -> Unit) {
    Page(title = "邀请码管理", contentPadding = contentPadding, topLink = "‹ 用户管理", onTopLink = onBack) {
        SegmentedTabs(listOf("用户列表", "邀请码"), selected = 1)
        AppCard {
            Text("Owner 可无限生成邀请码", fontWeight = FontWeight.Bold)
            MutedText("生成后可复制给同事注册；注册时会记录邀请关系。")
            PrimaryButton("＋ 生成邀请码", onUsers)
        }
        PhoneLoanData.repository.inviteCodes().forEach { code ->
            AppCard {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(code.code, fontWeight = FontWeight.Bold)
                    StatusPill(
                        text = when (code.status) {
                            InviteStatus.UNUSED -> "未使用"
                            InviteStatus.USED -> "已使用"
                            InviteStatus.EXPIRED -> "已过期"
                        },
                        color = when (code.status) {
                            InviteStatus.UNUSED -> AppColors.Success
                            InviteStatus.USED -> AppColors.Primary
                            InviteStatus.EXPIRED -> AppColors.Error
                        }
                    )
                }
                MutedText(code.detail)
            }
        }
    }
}
