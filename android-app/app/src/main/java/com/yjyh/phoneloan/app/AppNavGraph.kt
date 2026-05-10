package com.yjyh.phoneloan.app

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.yjyh.phoneloan.feature.auth.LoginScreen
import com.yjyh.phoneloan.feature.auth.RegisterScreen
import com.yjyh.phoneloan.feature.devices.DeviceDetailScreen
import com.yjyh.phoneloan.feature.devices.DevicesScreen
import com.yjyh.phoneloan.feature.home.HomeScreen
import com.yjyh.phoneloan.feature.owner.OwnerInvitesScreen
import com.yjyh.phoneloan.feature.owner.OwnerUsersScreen
import com.yjyh.phoneloan.feature.profile.ProfileScreen
import com.yjyh.phoneloan.feature.registerdevice.RegisterDeviceScreen
import com.yjyh.phoneloan.feature.returnloan.ReturnLoanScreen
import com.yjyh.phoneloan.feature.scanborrow.ScanBorrowScreen

@Composable
fun AppNavGraph(navController: NavHostController, contentPadding: PaddingValues) {
    NavHost(
        navController = navController,
        startDestination = AppRoute.Login.value
    ) {
        composable(AppRoute.Login.value) {
            LoginScreen(
                contentPadding = contentPadding,
                onLogin = { navController.navigate(AppRoute.Home.value) },
                onRegister = { navController.navigate(AppRoute.Register.value) }
            )
        }
        composable(AppRoute.Register.value) {
            RegisterScreen(contentPadding = contentPadding, onBack = { navController.popBackStack() })
        }
        composable(AppRoute.Home.value) {
            HomeScreen(
                contentPadding = contentPadding,
                onScanBorrow = { navController.navigate(AppRoute.ScanBorrow.value) },
                onReturnLoan = { navController.navigate(AppRoute.ReturnLoan.value) },
                onDevices = { navController.navigate(AppRoute.Devices.value) }
            )
        }
        composable(AppRoute.ScanBorrow.value) {
            ScanBorrowScreen(
                contentPadding = contentPadding,
                onBack = { navController.popBackStack() },
                onRegisterDevice = { imei ->
                    navController.navigate(AppRoute.RegisterDevice.create(imei))
                }
            )
        }
        composable(
            route = AppRoute.RegisterDevice.value,
            arguments = listOf(navArgument("imei") { type = NavType.StringType })
        ) { entry ->
            RegisterDeviceScreen(
                contentPadding = contentPadding,
                imei = entry.arguments?.getString("imei").orEmpty(),
                onBack = { navController.popBackStack() }
            )
        }
        composable(AppRoute.ReturnLoan.value) {
            ReturnLoanScreen(contentPadding = contentPadding, onBack = { navController.popBackStack() })
        }
        composable(AppRoute.Devices.value) {
            DevicesScreen(
                contentPadding = contentPadding,
                onAddDevice = { navController.navigate(AppRoute.ScanBorrow.value) },
                onOpenDevice = { navController.navigate(AppRoute.DeviceDetail.create(it)) }
            )
        }
        composable(AppRoute.DeviceDetail.value) {
            DeviceDetailScreen(contentPadding = contentPadding, onBack = { navController.popBackStack() })
        }
        composable(AppRoute.Profile.value) {
            ProfileScreen(
                contentPadding = contentPadding,
                onOwnerUsers = { navController.navigate(AppRoute.OwnerUsers.value) }
            )
        }
        composable(AppRoute.OwnerUsers.value) {
            OwnerUsersScreen(
                contentPadding = contentPadding,
                onBack = { navController.popBackStack() },
                onInvites = { navController.navigate(AppRoute.OwnerInvites.value) }
            )
        }
        composable(AppRoute.OwnerInvites.value) {
            OwnerInvitesScreen(
                contentPadding = contentPadding,
                onBack = { navController.popBackStack() },
                onUsers = { navController.navigate(AppRoute.OwnerUsers.value) }
            )
        }
    }
}
