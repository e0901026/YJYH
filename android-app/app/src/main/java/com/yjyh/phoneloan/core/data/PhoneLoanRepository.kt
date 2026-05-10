package com.yjyh.phoneloan.core.data

import com.yjyh.phoneloan.core.model.Device
import com.yjyh.phoneloan.core.model.InviteCode
import com.yjyh.phoneloan.core.model.LoanRecord
import com.yjyh.phoneloan.core.model.OwnerUserRow
import com.yjyh.phoneloan.core.model.User

interface PhoneLoanRepository {
    fun currentUser(): User
    fun devices(): List<Device>
    fun activeLoans(): List<LoanRecord>
    fun ownerUsers(): List<OwnerUserRow>
    fun inviteCodes(): List<InviteCode>
}
