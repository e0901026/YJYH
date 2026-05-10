package com.yjyh.phoneloan

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.yjyh.phoneloan.app.PhoneLoanApp
import com.yjyh.phoneloan.core.design.PhoneLoanTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PhoneLoanTheme {
                PhoneLoanApp()
            }
        }
    }
}
