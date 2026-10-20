package com.pnc.jetpackcomposedemos.core

import com.pnc.jetpackcomposedemos.BuildConfig
import com.pnc.jetpackcomposedemos.features.auth.domain.SessionManager
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val session: SessionManager
): Interceptor {

    private val apiHost = BuildConfig.AW_BASE_URL.toHttpUrl().host

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val token = session.currentUser?.token

        // The same OkHttpClient is shared with other servers (e.g. the artists API). Never send
        // our token to a host that isn't ours.
        if (token == null || request.url.host != apiHost) {
            return chain.proceed(request)
        }

        return chain.proceed(
            request.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        )
    }

}