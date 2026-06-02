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
import com.yjyh.phoneloan.core.design.InteractiveField
import com.yjyh.phoneloan.core.design.MutedText
import com.yjyh.phoneloan.core.design.Page
import com.yjyh.phoneloan.core.design.PrimaryButton
import com.yjyh.phoneloan.core.design.SecondaryButton
import com.yjyh.phoneloan.core.design.SegmentedTabs
import com.yjyh.phoneloan.core.design.StatusPill
import com.yjyh.phoneloan.core.model.InviteStatus
import com.yjyh.phoneloan.core.model.OwnerUserRow
import com.yjyh.phoneloan.core.model.UserRole

@Composable
fun OwnerUsersScreen(contentPadding: PaddingValues, onBack: () -> Unit, onInvites: () -> Unit) {
    val repository = PhoneLoanData.repository
    var operationMessage by remember { mutableStateOf("") }
    var editingUser by remember { mutableStateOf<OwnerUserRow?>(null) }
    var employeeNo by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var roleIndex by remember { mutableStateOf(0) }
    val selectedRole = if (roleIndex == 1) UserRole.OWNER else UserRole.USER
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
        AppCard {
            Text(if (editingUser == null) "新增用户" else "编辑用户", fontWeight = FontWeight.Bold)
            InteractiveField("工号", employeeNo, { employeeNo = it }, "例如 30001")
            InteractiveField("名称", name, { name = it }, "例如 张三")
            InteractiveField("密码", password, { password = it }, if (editingUser == null) "初始密码" else "留空则不修改")
            SegmentedTabs(listOf("普通用户", "Owner"), selected = roleIndex, onSelected = { roleIndex = it })
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PrimaryButton(if (editingUser == null) "新增用户" else "保存修改", {
                    val result = editingUser?.let {
                        repository.ownerUpdateUser(it.id, name, password, selectedRole)
                    } ?: repository.ownerCreateUser(employeeNo, name, password, selectedRole)
                    result
                        .onSuccess {
                            operationMessage = if (editingUser == null) "已新增用户：${it.name}" else "已保存用户：${it.name}"
                            editingUser = null
                            employeeNo = ""
                            name = ""
                            password = ""
                            roleIndex = 0
                            AnalyticsLogger.trackAction("owner_user_save_click", screen = "owner_users")
                        }
                        .onFailure { operationMessage = it.message ?: "用户保存失败" }
                }, Modifier.weight(1f))
                SecondaryButton("清空", {
                    editingUser = null
                    employeeNo = ""
                    name = ""
                    password = ""
                    roleIndex = 0
                    operationMessage = ""
                }, Modifier.weight(1f))
            }
        }
        AppCard {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("工号/名称", color = AppColors.Primary, fontWeight = FontWeight.Bold)
                Text("注册时间", color = AppColors.Primary, fontWeight = FontWeight.Bold)
                Text("邀请人", color = AppColors.Primary, fontWeight = FontWeight.Bold)
            }
            repository.ownerUsers().forEach { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    MutedText("${row.employeeNo}\n${row.name}")
                    MutedText(row.registeredAt)
                    MutedText(row.inviter)
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    StatusPill(if (row.role == UserRole.OWNER) "Owner" else "普通用户", AppColors.Primary)
                    StatusPill(if (row.enabled) "启用" else "已停用", if (row.enabled) AppColors.Success else AppColors.Error)
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SecondaryButton("编辑", {
                        editingUser = row
                        employeeNo = row.employeeNo
                        name = row.name
                        password = ""
                        roleIndex = if (row.role == UserRole.OWNER) 1 else 0
                        operationMessage = "正在编辑：${row.name}"
                        AnalyticsLogger.trackAction("owner_user_edit_click", screen = "owner_users")
                    }, Modifier.weight(1f))
                    SecondaryButton("停用/删除", {
                        repository.ownerDisableUser(row.id)
                            .onSuccess { operationMessage = "已停用用户：${it.name}" }
                            .onFailure { operationMessage = it.message ?: "停用用户失败" }
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
    val repository = PhoneLoanData.repository
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
                repository.ownerCreateInviteCode()
                    .onSuccess {
                        generatedMessage = "已生成新邀请码：${it.code}"
                        AnalyticsLogger.trackAction("owner_invite_generate_click", screen = "owner_invites")
                    }
                    .onFailure { generatedMessage = it.message ?: "生成邀请码失败" }
            })
            if (generatedMessage.isNotBlank()) {
                Text(generatedMessage, color = AppColors.Primary, fontWeight = FontWeight.Bold)
            }
        }
        repository.inviteCodes().forEach { code ->
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
