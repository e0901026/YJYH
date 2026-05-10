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

    private val deviceList = listOf(
        Device("d1", "小米14 白", "869301065812347", null, meSummary, li, DeviceStatus.BORROWED_OUT),
        Device("d2", "OPPO Find X7", "866001123456789", "866001123456797", meSummary, meSummary, DeviceStatus.HELD_BY_ME),
        Device("d3", "iPhone 15 Pro", "867450991234568", null, han, meSummary, DeviceStatus.PENDING_RETURN)
    )

    override fun currentUser() = me

    override fun devices() = deviceList

    override fun activeLoans() = listOf(
        LoanRecord("l1", deviceList[0], li, "今天 10:24", "我借出去的"),
        LoanRecord("l2", deviceList[2], han, "昨天 18:01", "我借入的")
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
}
