package com.yjyh.phoneloan.feature.scanborrow

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ImeiParserTest {
    @Test
    fun validImeiRequires15DigitsOnly() {
        assertTrue(ImeiParser.isValid("869301065812347"))
        assertFalse(ImeiParser.isValid("86930106581234"))
        assertFalse(ImeiParser.isValid("8693010658123478"))
        assertFalse(ImeiParser.isValid("IMEI 869301065812347"))
    }

    @Test
    fun extractsFirst15DigitImeiFromBarcodeText() {
        assertEquals(
            "869301065812347",
            ImeiParser.extractFirst(listOf("IMEI: 869301065812347", "866001123456789"))
        )
    }

    @Test
    fun ignoresNumbersEmbeddedInLongerDigitRuns() {
        assertNull(ImeiParser.extractFirst(listOf("x8693010658123478x")))
    }

    @Test
    fun returnsNullWhenNoImeiExists() {
        assertNull(ImeiParser.extractFirst(listOf("asset=phone", "hello")))
    }
}
