package com.pnc.jetpackcomposedemos.core.observability


import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LogRedactorTest {

    @Test
    fun `a bearer token is hidden`() {
        val result = LogRedactor.scrub("Authorization: Bearer abc.DEF-123_xyz")

        assertEquals("Authorization: Bearer [redacted]", result)
    }

    @Test
    fun `a bare JWT is hidden`() {
        val jwt = "eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiIxMTAwMCJ9.c2lnbmF0dXJl"

        val result = LogRedactor.scrub("token was $jwt for the call")

        assertEquals("token was [jwt] for the call", result)
    }

    @Test
    fun `secrets in a query string or form body are hidden but other parameters stay`() {
        val result = LogRedactor.scrub("POST grant_type=authorization_code&code=SECRET1&code_verifier=SECRET2&client_id=mobile")

        assertEquals("POST grant_type=authorization_code&code=[redacted]&code_verifier=[redacted]&client_id=mobile", result)
    }

    @Test
    fun `a plain token parameter is hidden`() {
        assertEquals("failed for token=[redacted] twice", LogRedactor.scrub("failed for token=abc123 twice"))
    }

    @Test
    fun `secrets in a JSON body are hidden`() {
        val result = LogRedactor.scrub("""{"access_token":"AAA","refresh_token":"BBB","expires_in":300}""")

        assertFalse(result.contains("AAA"))
        assertFalse(result.contains("BBB"))
        assertTrue(result.contains("\"expires_in\":300"))
    }

    @Test
    fun `an email address is hidden`() {
        assertEquals("sent to [email] today", LogRedactor.scrub("sent to cara.cole@example.com today"))
    }

    @Test
    fun `text with nothing sensitive is unchanged`() {
        val text = "Refreshing the artists failed: timeout after 10000 ms"

        assertEquals(text, LogRedactor.scrub(text))
    }
}


