package com.yjyh.phoneloan.core.data

object PhoneLoanData {
    val repository: PhoneLoanRepository = RemotePhoneLoanRepository(MockPhoneLoanRepository)
}
