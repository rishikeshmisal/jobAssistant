package com.jobassistant.data.repository

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class GmailAuthExpiredExceptionTest {

    @Test
    fun `default message is set`() {
        val ex = GmailAuthExpiredException()
        assertNotNull(ex.message)
        assertTrue(ex.message!!.contains("expired"))
    }

    @Test
    fun `custom message is preserved`() {
        val ex = GmailAuthExpiredException("custom error message")
        assertEquals("custom error message", ex.message)
    }

    @Test
    fun `is subtype of IOException`() {
        val ex = GmailAuthExpiredException()
        assertTrue(ex is IOException)
    }

    @Test
    fun `can be caught as IOException`() {
        var caught: IOException? = null
        try {
            throw GmailAuthExpiredException()
        } catch (e: IOException) {
            caught = e
        }
        assertNotNull(caught)
        assertTrue(caught is GmailAuthExpiredException)
    }
}
