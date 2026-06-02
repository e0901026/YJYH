package com.yjyh.phoneloan.feature.owner

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yjyh.phoneloan.core.analytics.AnalyticsLogger
import com.yjyh.phoneloan.core.data.PhoneLoanData
import com.yjyh.phoneloan.core.design.AppCard
import com.yjyh.phoneloan.core.design.AppColors
import com.yjyh.phoneloan.core.design.Field
import com.yjyh.phoneloan.core.design.MutedText
import com.yjyh.phoneloan.core.design.Page
import com.yjyh.phoneloan.core.design.PrimaryButton
import com.yjyh.phoneloan.core.design.SecondaryButton
import com.yjyh.phoneloan.core.design.SegmentedTabs
import com.yjyh.phoneloan.core.design.StatusPill
import com.yjyh.phoneloan.core.model.InviteStatus

@Composable
fun OwnerUsersScreen(contentPadding: PaddingValues, onBack: () -> Unit, onInvites: () -> Unit) {
    var operationMessage by remember { mutableStateOf("") }
    Page(title = "用户管理", contentPadding = contentPadding, topLink = "‹ 我的", onTopLink = onBack) {
        SegmentedTabs(
            listOf("用户列表", "邀请码"),
            selected = 0,
            onSelected = { index ->
                if (index == 1) {
                    AnalyticsLogger.trackAction("owner_invite_tab_click", screen = "owner_users")
                    onInvites()
                }
            }
        )
        Field(label = "", placeholder = "搜索工号 / 名称")
        PrimaryButton("新增用户", {
            operationMessage = "新增用户入口已打开：后续版本接入真实表单和后端创建接口。"
            AnalyticsLogger.trackAction("owner_user_create_click", screen = "owner_users")
        })
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
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SecondaryButton("编辑", {
                        operationMessage = "编辑用户：${row.name}"
                        AnalyticsLogger.trackAction("owner_user_edit_click", screen = "owner_users")
                    }, Modifier.weight(1f))
                    SecondaryButton("停用/删除", {
                        operationMessage = "停用/删除用户：${row.name}"
                        AnalyticsLogger.trackAction("owner_user_disable_click", screen = "owner_users")
                    }, Modifier.weight(1f))
                }
            }
        }
        AppCard {
            Text("管理员能力", fontWeight = FontWeight.Bold)
            MutedText("当前管理员账号可维护用户列表；邀请码页可无限生成邀请码。")
            if (operationMessage.isNotBlank()) {
                Text(operationMessage, color = AppColors.Primary, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun OwnerInvitesScreen(contentPadding: PaddingValues, onBack: () -> Unit, onUsers: () -> Unit) {
    var generatedMessage by remember { mutableStateOf("") }
    Page(title = "邀请码管理", contentPadding = contentPadding, topLink = "‹ 用户管理", onTopLink = onBack) {
        SegmentedTabs(
            listOf("用户列表", "邀请码"),
            selected = 1,
            onSelected = { index ->
                if (index == 0) {
                    AnalyticsLogger.trackAction("owner_user_tab_click", screen = "owner_invites")
                    onUsers()
                }
            }
        )
        AppCard {
            Text("Owner 可无限生成邀请码", fontWeight = FontWeight.Bold)
            MutedText("生成后可复制给同事注册；注册时会记录邀请关系。")
            PrimaryButton("＋ 生成邀请码", {
                generatedMessage = "已生成新邀请码：YJYH-NEW${(100..999).random()}"
                AnalyticsLogger.trackAction("owner_invite_generate_click", screen = "owner_invites")
            })
            if (generatedMessage.isNotBlank()) {
                Text(generatedMessage, color = AppColors.Primary, fontWeight = FontWeight.Bold)
            }
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
