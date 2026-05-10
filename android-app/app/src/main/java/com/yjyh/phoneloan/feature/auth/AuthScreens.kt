package com.yjyh.phoneloan.feature.auth

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import com.yjyh.phoneloan.core.design.AppCard
import com.yjyh.phoneloan.core.design.Field
import com.yjyh.phoneloan.core.design.MutedText
import com.yjyh.phoneloan.core.design.Page
import com.yjyh.phoneloan.core.design.PrimaryButton
import com.yjyh.phoneloan.core.design.SecondaryButton

@Composable
fun LoginScreen(contentPadding: PaddingValues, onLogin: () -> Unit, onRegister: () -> Unit) {
    Page(title = "手机借机管理", contentPadding = contentPadding) {
        MutedText("登记、扫码借走、归还，一眼看清手上持有台数")
        AppCard {
            Field(label = "工号", placeholder = "请输入工号")
            Field(label = "密码", placeholder = "请输入密码")
            PrimaryButton("登录", onLogin)
            SecondaryButton("使用邀请码注册", onRegister)
        }
    }
}

@Composable
fun RegisterScreen(contentPadding: PaddingValues, onBack: () -> Unit) {
    Page(title = "注册账号", contentPadding = contentPadding, topLink = "‹ 登录", onTopLink = onBack) {
        MutedText("必须持有有效邀请码；同一工号只能注册一次。")
        AppCard {
            Field(label = "邀请码", placeholder = "例如 YJYH-8K2P")
            Field(label = "工号", placeholder = "全局唯一")
            Field(label = "名称", placeholder = "展示名")
            Field(label = "密码", placeholder = "请输入密码")
            Field(label = "确认密码", placeholder = "再次输入密码")
            PrimaryButton("注册", onBack)
        }
    }
}
