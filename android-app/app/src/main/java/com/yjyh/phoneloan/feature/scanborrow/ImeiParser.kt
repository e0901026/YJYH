package com.yjyh.phoneloan.feature.scanborrow

object ImeiParser {
    private val imeiPattern = Regex("""(?<!\d)\d{15}(?!\d)""")

    fun isValid(imei: String): Boolean = imeiPattern.matches(imei.trim())

    fun extractFirst(rawValues: Iterable<String>): String? {
        return rawValues
            .asSequence()
            .mapNotNull { imeiPattern.find(it)?.value }
            .firstOrNull()
    }
}
