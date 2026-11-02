package com.pnc.jetpackcomposedemos.core.observability


import okhttp3.logging.HttpLoggingInterceptor
import timber.log.Timber

object HttpLogging {

    /**
     * An HTTP logger that never prints the headers that carry credentials. Without redactHeader the BODY level
     * prints "Authorization: Bearer <the real token>" for every request, straight into Logcat.
     */
    fun interceptor(
        level: HttpLoggingInterceptor.Level,
        logger: HttpLoggingInterceptor.Logger = HttpLoggingInterceptor.Logger { message ->
            Timber.tag("OkHttp").d(message)
        }
    ): HttpLoggingInterceptor =
        HttpLoggingInterceptor(logger).apply {
            this.level = level
            redactHeader("Authorization")
            redactHeader("Cookie")
            redactHeader("Set-Cookie")
        }
}



