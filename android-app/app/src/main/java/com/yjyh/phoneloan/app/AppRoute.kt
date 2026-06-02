package com.yjyh.phoneloan.app

sealed class AppRoute(val value: String) {
    data object Login : AppRoute("login")
    data object Register : AppRoute("register")
    data object Home : AppRoute("home")
    data object ScanBorrow : AppRoute("scanBorrow")
    data object RegisterDevice : AppRoute("registerDevice/{imei}") {
        fun create(imei: String) = "registerDevice/$imei"
    }
    data object ReturnLoan : AppRoute("returnLoan")
    data object Devices : AppRoute("devices")
    data object DevicesOnHand : AppRoute("devices/onHand")
    data object DeviceDetail : AppRoute("deviceDetail/{id}") {
        fun create(id: String) = "deviceDetail/$id"
    }
    data object Profile : AppRoute("profile")
    data object OwnerUsers : AppRoute("ownerUsers")
    data object OwnerInvites : AppRoute("ownerInvites")
}
