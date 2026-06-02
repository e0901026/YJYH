package com.yjyh.phoneloan.core.model

enum class UserRole { USER, OWNER }
enum class DeviceStatus { HELD_BY_ME, BORROWED_OUT, PENDING_RETURN, AVAILABLE }
enum class InviteStatus { UNUSED, USED, EXPIRED }

data class UserSummary(
    val id: String,
    val employeeNo: String,
    val name: String
)

data class User(
    val id: String,
    val employeeNo: String,
    val name: String,
    val role: UserRole,
    val inviteUsed: Int,
    val inviteLimit: Int = 10
)

data class Device(
    val id: String,
    val name: String,
    val imei1: String,
    val imei2: String?,
    val owner: UserSummary,
    val currentHolder: UserSummary?,
    val status: DeviceStatus,
    val latestEventLabel: String = "",
    val latestEventOrder: Long = 0
)

data class LoanRecord(
    val id: String,
    val device: Device,
    val counterpart: UserSummary,
    val startedAt: String,
    val statusText: String
)

data class InviteCode(
    val id: String,
    val code: String,
    val status: InviteStatus,
    val detail: String
)

data class OwnerUserRow(
    val id: String,
    val employeeNo: String,
    val name: String,
    val registeredAt: String,
    val inviter: String,
    val role: UserRole = UserRole.USER,
    val enabled: Boolean = true
)
