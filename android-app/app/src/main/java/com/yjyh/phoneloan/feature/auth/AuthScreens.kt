package com.yjyh.phoneloan.feature.auth

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import com.yjyh.phoneloan.core.analytics.AnalyticsLogger
import com.yjyh.phoneloan.core.data.PhoneLoanData
import com.yjyh.phoneloan.core.design.AppCard
import com.yjyh.phoneloan.core.design.AppColors
import com.yjyh.phoneloan.core.design.InteractiveField
import com.yjyh.phoneloan.core.design.MutedText
import com.yjyh.phoneloan.core.design.Page
import com.yjyh.phoneloan.core.design.PrimaryButton
import com.yjyh.phoneloan.core.design.SecondaryButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun LoginScreen(contentPadding: PaddingValues, onLogin: () -> Unit, onRegister: () -> Unit) {
    val scope = rememberCoroutineScope()
    var employeeNo by remember { mutableStateOf("10086") }
    var password by remember { mutableStateOf("password123") }
    var errorMessage by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }

    fun submitLogin() {
        val trimmedNo = employeeNo.trim()
        if (trimmedNo.isBlank() || password.isBlank()) {
            errorMessage = "请输入工号和密码"
            AnalyticsLogger.trackError("login_validation_failed", screen = "login")
            return
        }
        loading = true
        errorMessage = ""
        AnalyticsLogger.trackAction("login_submit", screen = "login", payload = mapOf("employeeNo" to trimmedNo))
        scope.launch {
            val result = withContext(Dispatchers.IO) {
                PhoneLoanData.repository.login(trimmedNo, password)
            }
            loading = false
            result
                .onSuccess { onLogin() }
                .onFailure { errorMessage = it.message ?: "登录失败，请稍后再试" }
        }
    }

    Page(title = "手机借机管理", contentPadding = contentPadding) {
        MutedText("登记、扫码借走、归还，一眼看清手上持有台数")
        AppCard {
            InteractiveField(
                label = "工号",
                value = employeeNo,
                onValueChange = {
                    employeeNo = it
                    errorMessage = ""
                },
                placeholder = "请输入工号",
                keyboardType = KeyboardType.Number
            )
            InteractiveField(
                label = "密码",
                value = password,
                onValueChange = {
                    password = it
                    errorMessage = ""
                },
                placeholder = "请输入密码",
                keyboardType = KeyboardType.Password,
                visualTransformation = PasswordVisualTransformation()
            )
            if (errorMessage.isNotEmpty()) {
                TextError(errorMessage)
            }
            PrimaryButton("登录", onClick = {
                if (!loading) submitLogin()
            })
            SecondaryButton("使用邀请码注册", onClick = {
                AnalyticsLogger.trackAction("register_entry_click", screen = "login")
                onRegister()
            })
        }
    }
}

@Composable
fun RegisterScreen(contentPadding: PaddingValues, onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    var inviteCode by remember { mutableStateOf("OWNER-SEED-0001") }
    var employeeNo by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }

    fun submitRegister() {
        val trimmedInvite = inviteCode.trim()
        val trimmedNo = employeeNo.trim()
        val trimmedName = name.trim()
        when {
            trimmedInvite.isBlank() || trimmedNo.isBlank() || trimmedName.isBlank() || password.isBlank() || confirmPassword.isBlank() -> {
                errorMessage = "请完整填写注册信息"
            }
            password != confirmPassword -> {
                errorMessage = "两次输入的密码不一致"
            }
            password.length < 6 -> {
                errorMessage = "密码至少 6 位"
            }
            else -> {
                loading = true
                errorMessage = ""
                AnalyticsLogger.trackAction("register_submit", screen = "register", payload = mapOf("employeeNo" to trimmedNo))
                scope.launch {
                    val result = withContext(Dispatchers.IO) {
                        PhoneLoanData.repository.register(trimmedNo, trimmedName, password, trimmedInvite)
                    }
                    loading = false
                    result
                        .onSuccess { onBack() }
                        .onFailure { errorMessage = it.message ?: "注册失败，请稍后再试" }
                }
                return
            }
        }
        AnalyticsLogger.trackError("register_validation_failed", screen = "register", payload = mapOf("employeeNo" to trimmedNo))
    }

    Page(title = "注册账号", contentPadding = contentPadding, topLink = "‹ 登录", onTopLink = onBack) {
        MutedText("必须持有有效邀请码；同一工号只能注册一次。")
        AppCard {
            InteractiveField(
                label = "邀请码",
                value = inviteCode,
                onValueChange = {
                    inviteCode = it
                    errorMessage = ""
                },
                placeholder = "例如 YJYH-8K2P"
            )
            InteractiveField(
                label = "工号",
                value = employeeNo,
                onValueChange = {
                    employeeNo = it
                    errorMessage = ""
                },
                placeholder = "全局唯一",
                keyboardType = KeyboardType.Number
            )
            InteractiveField(
                label = "名称",
                value = name,
                onValueChange = {
                    name = it
                    errorMessage = ""
                },
                placeholder = "展示名"
            )
            InteractiveField(
                label = "密码",
                value = password,
                onValueChange = {
                    password = it
                    errorMessage = ""
                },
                placeholder = "请输入密码",
                keyboardType = KeyboardType.Password,
                visualTransformation = PasswordVisualTransformation()
            )
            InteractiveField(
                label = "确认密码",
                value = confirmPassword,
                onValueChange = {
                    confirmPassword = it
                    errorMessage = ""
                },
                placeholder = "再次输入密码",
                keyboardType = KeyboardType.Password,
                visualTransformation = PasswordVisualTransformation()
            )
            if (errorMessage.isNotEmpty()) {
                TextError(errorMessage)
            }
            PrimaryButton("注册", onClick = {
                if (!loading) submitRegister()
            })
        }
    }
}

@Composable
private fun TextError(message: String) {
    androidx.compose.material3.Text(
        text = message,
        color = AppColors.Error,
        fontWeight = FontWeight.Bold
    )
}
