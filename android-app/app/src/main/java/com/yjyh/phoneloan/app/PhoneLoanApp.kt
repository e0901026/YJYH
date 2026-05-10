package com.yjyh.phoneloan.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.yjyh.phoneloan.core.design.PhoneLoanBackground
import com.yjyh.phoneloan.core.design.PhoneLoanBottomBar

@Composable
fun PhoneLoanApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val route = backStackEntry?.destination?.route
    val showBottomBar = route in setOf(
        AppRoute.Home.value,
        AppRoute.Devices.value,
        AppRoute.Profile.value
    )

    PhoneLoanBackground {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = com.yjyh.phoneloan.core.design.AppColors.Page,
            bottomBar = {
                if (showBottomBar) {
                    PhoneLoanBottomBar(
                        currentRoute = route,
                        onNavigate = { target ->
                            navController.navigate(target.value) {
                                popUpTo(AppRoute.Home.value) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        ) { padding ->
            AppNavGraph(navController = navController, contentPadding = padding)
        }
    }
}
