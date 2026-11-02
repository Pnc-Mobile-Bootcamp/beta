package com.pnc.jetpackcomposedemos.core.observability


import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HttpLoggingTest {

    // A "server" that answers without any network, so the test only exercises the logger.
    private val cannedResponse = Interceptor { chain ->
        Response.Builder()
            .request(chain.request())
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .body("""{"ok":true}""".toResponseBody())
            .build()
    }

    private fun linesLoggedFor(request: Request, level: HttpLoggingInterceptor.Level): List<String> {
        val lines = mutableListOf<String>()
        val client = OkHttpClient.Builder()
            .addInterceptor(HttpLogging.interceptor(level) { lines += it })
            .addInterceptor(cannedResponse)
            .build()
        client.newCall(request).execute().close()
        return lines
    }

    @Test
    fun `the authorization header value never appears in the log`() {
        val request = Request.Builder()
            .url("https://api.example.test/orders")
            .header("Authorization", "Bearer SECRET-TOKEN-123")
            .build()

        val lines = linesLoggedFor(request, HttpLoggingInterceptor.Level.BODY)

        assertFalse(lines.any { it.contains("SECRET-TOKEN-123") })
        assertTrue(lines.any { it.startsWith("Authorization:") })   // the header is listed, its value is replaced
    }

    @Test
    fun `an ordinary header is still logged in full`() {
        val request = Request.Builder()
            .url("https://api.example.test/orders")
            .header("Accept", "application/json")
            .build()

        val lines = linesLoggedFor(request, HttpLoggingInterceptor.Level.HEADERS)

        assertTrue(lines.any { it.contains("Accept: application/json") })
    }
}



