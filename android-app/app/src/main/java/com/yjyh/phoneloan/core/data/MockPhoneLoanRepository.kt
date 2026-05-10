package com.yjyh.phoneloan.core.data

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

    private val _devices = mutableListOf(
        Device("d1", "小米14 白", "869301065812347", null, meSummary, li, DeviceStatus.BORROWED_OUT),
        Device("d2", "OPPO Find X7", "866001123456789", "866001123456797", meSummary, meSummary, DeviceStatus.HELD_BY_ME),
        Device("d3", "iPhone 15 Pro", "867450991234568", null, han, meSummary, DeviceStatus.PENDING_RETURN)
    )

    private var _nextDeviceId = 4

    override fun currentUser() = me

    override fun devices() = _devices.toList()

    override fun activeLoans() = listOf(
        LoanRecord("l1", _devices[0], li, "今天 10:24", "我借出去的"),
        LoanRecord("l2", _devices[2], han, "昨天 18:01", "我借入的")
    )

    override fun ownerUsers() = listOf(
        OwnerUserRow("10086", "王晓明", "2026-05-08", "Owner 00001"),
        OwnerUserRow("10248", "李雷", "2026-05-09", "王晓明 10086")
    )

    override fun inviteCodes() = listOf(
        InviteCode("i1", "YJYH-8K2P", InviteStatus.UNUSED, "创建：王晓明 · 2026-05-10"),
        InviteCode("i2", "YJYH-4N7Q", InviteStatus.USED, "使用者：李雷 · 10248"),
        InviteCode("i3", "YJYH-OP6M", InviteStatus.EXPIRED, "过期：2026-06-10")
    )

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
        return device
    }

    override fun updateDeviceHolder(deviceId: String, newHolder: UserSummary, newStatus: DeviceStatus) {
        val index = _devices.indexOfFirst { it.id == deviceId }
        if (index >= 0) {
            _devices[index] = _devices[index].copy(
                currentHolder = newHolder,
                status = newStatus
            )
        }
    }
}
