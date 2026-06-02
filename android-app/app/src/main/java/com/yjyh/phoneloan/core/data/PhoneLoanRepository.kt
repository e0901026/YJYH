package com.yjyh.phoneloan.core.data

import com.yjyh.phoneloan.core.model.Device
import com.yjyh.phoneloan.core.model.DeviceStatus
import com.yjyh.phoneloan.core.model.InviteCode
import com.yjyh.phoneloan.core.model.LoanRecord
import com.yjyh.phoneloan.core.model.OwnerUserRow
import com.yjyh.phoneloan.core.model.User
import com.yjyh.phoneloan.core.model.UserRole
import com.yjyh.phoneloan.core.model.UserSummary

interface PhoneLoanRepository {
    fun login(employeeNo: String, password: String): Result<Unit>
    fun register(employeeNo: String, name: String, password: String, inviteCode: String): Result<Unit>
    fun currentUser(): User
    fun devices(): List<Device>
    fun activeLoans(): List<LoanRecord>
    fun ownerUsers(): List<OwnerUserRow>
    fun inviteCodes(): List<InviteCode>
    fun latestActivity(): String

    fun ownerCreateUser(employeeNo: String, name: String, password: String, role: UserRole): Result<OwnerUserRow> =
        Result.failure(UnsupportedOperationException("当前数据源暂不支持新增用户"))

    fun ownerUpdateUser(userId: String, name: String, password: String, role: UserRole): Result<OwnerUserRow> =
        Result.failure(UnsupportedOperationException("当前数据源暂不支持编辑用户"))

    fun ownerDisableUser(userId: String): Result<OwnerUserRow> =
        Result.failure(UnsupportedOperationException("当前数据源暂不支持停用用户"))

    fun ownerCreateInviteCode(): Result<InviteCode> =
        Result.failure(UnsupportedOperationException("当前数据源暂不支持生成邀请码"))

    /** 根据 IMEI1 查找设备，找不到返回 null */
    fun findDeviceByImei(imei: String): Device?

    /** 注册新设备 */
    fun addDevice(name: String, imei: String): Device
    fun addDeviceResult(name: String, imei: String): Result<Device> = runCatching { addDevice(name, imei) }

    /** 更新设备持有人 */
    fun updateDeviceHolder(deviceId: String, newHolder: UserSummary, newStatus: DeviceStatus)
    fun borrowDeviceResult(deviceId: String, newHolder: UserSummary, newStatus: DeviceStatus): Result<Unit> = runCatching {
        updateDeviceHolder(deviceId, newHolder, newStatus)
    }

    /** 结束一条 mock 借还记录 */
    fun returnLoan(deviceId: String)
    fun returnLoanResult(deviceId: String): Result<Unit> = runCatching { returnLoan(deviceId) }

    fun urgeReturn(deviceId: String)
    fun urgeReturnResult(deviceId: String): Result<Unit> = runCatching { urgeReturn(deviceId) }

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
