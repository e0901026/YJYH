package com.yjyh.phoneloan.core.data

import com.yjyh.phoneloan.core.model.Device
import com.yjyh.phoneloan.core.model.DeviceStatus
import com.yjyh.phoneloan.core.model.InviteCode
import com.yjyh.phoneloan.core.model.LoanRecord
import com.yjyh.phoneloan.core.model.OwnerUserRow
import com.yjyh.phoneloan.core.model.User
import com.yjyh.phoneloan.core.model.UserSummary

interface PhoneLoanRepository {
    fun currentUser(): User
    fun devices(): List<Device>
    fun activeLoans(): List<LoanRecord>
    fun ownerUsers(): List<OwnerUserRow>
    fun inviteCodes(): List<InviteCode>
    fun latestActivity(): String

    /** 根据 IMEI1 查找设备，找不到返回 null */
    fun findDeviceByImei(imei: String): Device?

    /** 注册新设备 */
    fun addDevice(name: String, imei: String): Device

    /** 更新设备持有人 */
    fun updateDeviceHolder(deviceId: String, newHolder: UserSummary, newStatus: DeviceStatus)

    /** 结束一条 mock 借还记录 */
    fun returnLoan(deviceId: String)

    fun urgeReturn(deviceId: String)

    fun heldCount(): Int {
        return devices().count { it.currentHolder?.id == currentUser().id }
    }

    fun borrowedOutCount(): Int {
        val me = currentUser()
        return devices().count { it.owner.id == me.id && it.currentHolder?.id != me.id }
    }

    fun borrowedInCount(): Int {
        val me = currentUser()
        return devices().count { it.owner.id != me.id && it.currentHolder?.id == me.id }
    }
}
