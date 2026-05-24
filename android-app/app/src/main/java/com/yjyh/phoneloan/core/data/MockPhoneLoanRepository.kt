package com.yjyh.phoneloan.core.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.yjyh.phoneloan.core.analytics.AnalyticsLogger
import com.yjyh.phoneloan.core.model.Device
import com.yjyh.phoneloan.core.model.DeviceStatus
import com.yjyh.phoneloan.core.model.InviteCode
import com.yjyh.phoneloan.core.model.InviteStatus
import com.yjyh.phoneloan.core.model.LoanRecord
import com.yjyh.phoneloan.core.model.OwnerUserRow
import com.yjyh.phoneloan.core.model.User
import com.yjyh.phoneloan.core.model.UserRole
import com.yjyh.phoneloan.core.model.UserSummary

object MockPhoneLoanRepository : PhoneLoanRepository {
    private val me = User("u1", "10086", "王晓明", UserRole.OWNER, inviteUsed = 3)
    private val meSummary = UserSummary(me.id, me.employeeNo, me.name)
    private val li = UserSummary("u2", "10248", "李雷")
    private val han = UserSummary("u3", "10881", "韩梅梅")

    private val _devices = mutableStateListOf(
        Device("d1", "小米14 白", "869301065812347", null, meSummary, li, DeviceStatus.BORROWED_OUT),
        Device("d2", "OPPO Find X7", "866001123456789", "866001123456797", meSummary, meSummary, DeviceStatus.HELD_BY_ME),
        Device("d3", "iPhone 15 Pro", "867450991234568", null, han, meSummary, DeviceStatus.PENDING_RETURN)
    )

    private var _nextDeviceId = 4
    var latestActivity by mutableStateOf("小米14 白刚被李雷借走，已记录上一位持有人和绑定 owner。")
        private set

    override fun currentUser() = me

    init {
        AnalyticsLogger.identifyUser(me.id, me.employeeNo)
    }

    override fun devices() = _devices.toList()

    override fun activeLoans(): List<LoanRecord> {
        return _devices.mapNotNull { device ->
            val holder = device.currentHolder ?: return@mapNotNull null
            when {
                device.owner.id == me.id && holder.id != me.id -> {
                    LoanRecord(device.id, device, holder, "今天 10:24", "我借出去的")
                }
                device.owner.id != me.id && holder.id == me.id -> {
                    LoanRecord(device.id, device, device.owner, "昨天 18:01", "我借入的")
                }
                else -> null
            }
        }
    }

    override fun ownerUsers() = listOf(
        OwnerUserRow("10086", "王晓明", "2026-05-08", "Owner 00001"),
        OwnerUserRow("10248", "李雷", "2026-05-09", "王晓明 10086")
    )

    override fun inviteCodes() = listOf(
        InviteCode("i1", "YJYH-8K2P", InviteStatus.UNUSED, "创建：王晓明 · 2026-05-10"),
        InviteCode("i2", "YJYH-4N7Q", InviteStatus.USED, "使用者：李雷 · 10248"),
        InviteCode("i3", "YJYH-OP6M", InviteStatus.EXPIRED, "过期：2026-06-10")
    )

    override fun latestActivity() = latestActivity

    override fun findDeviceByImei(imei: String): Device? {
        return _devices.find { it.imei1 == imei || it.imei2 == imei }
    }

    override fun addDevice(name: String, imei: String): Device {
        val device = Device(
            id = "d${_nextDeviceId++}",
            name = name,
            imei1 = imei,
            imei2 = null,
            owner = meSummary,
            currentHolder = meSummary,
            status = DeviceStatus.HELD_BY_ME
        )
        _devices.add(device)
        latestActivity = "${device.name} 已建档，并记录你为当前持有人。"
        AnalyticsLogger.trackAction(
            name = "device_registered",
            screen = "register_device",
            payload = mapOf("deviceId" to device.id, "imei" to device.imei1)
        )
        return device
    }

    override fun updateDeviceHolder(deviceId: String, newHolder: UserSummary, newStatus: DeviceStatus) {
        val index = _devices.indexOfFirst { it.id == deviceId }
        if (index >= 0) {
            val oldDevice = _devices[index]
            val oldHolder = oldDevice.currentHolder
            _devices[index] = _devices[index].copy(
                currentHolder = newHolder,
                status = newStatus
            )
            latestActivity = buildString {
                append("${oldDevice.name} 已被${newHolder.name}借走")
                if (oldHolder != null && oldHolder.id != newHolder.id) {
                    append("，已通知上一位持有人${oldHolder.name}")
                }
                append("和绑定 owner ${oldDevice.owner.name}。")
            }
            AnalyticsLogger.trackAction(
                name = "device_borrowed",
                screen = "scan_borrow",
                payload = mapOf(
                    "deviceId" to oldDevice.id,
                    "previousHolderId" to oldHolder?.id,
                    "newHolderId" to newHolder.id,
                    "ownerId" to oldDevice.owner.id
                )
            )
        }
    }

    override fun returnLoan(deviceId: String) {
        val index = _devices.indexOfFirst { it.id == deviceId }
        if (index < 0) return

        val device = _devices[index]
        val nextHolder = device.owner
        val nextStatus = if (device.owner.id == me.id) {
            DeviceStatus.HELD_BY_ME
        } else {
            DeviceStatus.AVAILABLE
        }
        _devices[index] = device.copy(
            currentHolder = nextHolder,
            status = nextStatus
        )
        latestActivity = "${device.name} 已归还，当前持有人更新为 ${nextHolder.name}。"
        AnalyticsLogger.trackAction(
            name = "device_returned",
            screen = "return_loan",
            payload = mapOf("deviceId" to device.id, "ownerId" to device.owner.id)
        )
    }

    override fun urgeReturn(deviceId: String) {
        val device = _devices.find { it.id == deviceId } ?: return
        latestActivity = "${device.name} 已发送催还消息给 ${device.currentHolder?.name ?: "当前持有人"}。"
        AnalyticsLogger.trackAction(
            name = "device_urge_returned",
            screen = "return_loan",
            payload = mapOf("deviceId" to device.id, "holderId" to device.currentHolder?.id)
        )
    }

    override fun heldCount(): Int {
        return _devices.count { it.currentHolder?.id == me.id }
    }

    override fun borrowedOutCount(): Int {
        return _devices.count { it.owner.id == me.id && it.currentHolder?.id != me.id }
    }

    override fun borrowedInCount(): Int {
        return _devices.count { it.owner.id != me.id && it.currentHolder?.id == me.id }
    }
}
