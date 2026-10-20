package com.pnc.jetpackcomposedemos.core

import com.pnc.jetpackcomposedemos.BuildConfig
import com.pnc.jetpackcomposedemos.features.auth.data.oidc.OidcClient
import com.pnc.jetpackcomposedemos.features.auth.data.oidc.RefreshRejectedException
import com.pnc.jetpackcomposedemos.features.auth.domain.SessionManager
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class TokenAuthenticator @Inject constructor(
    private val session: SessionManager,
    private val oidc: OidcClient
) : Authenticator {

    private val apiHost = BuildConfig.AW_BASE_URL.toHttpUrl().host
    private val lock = Any()

    override fun authenticate(route: Route?, response: Response): Request? {
        if (response.request.url.host != apiHost) return null
        if (responseCount(response) >= 2) return null       // already retried once: give up, no loops

        // Only one refresh at a time. If three requests fail together, the first refreshes and the
        // others find the new token waiting. The old refresh token can only be used once.
        synchronized(lock) {
            val user = session.currentUser ?: return null
            val sentToken = response.request.header("Authorization")?.removePrefix("Bearer ")

            if (user.token != sentToken) {
                // Someone else already refreshed while we waited for the lock.
                return withToken(response.request, user.token)
            }

            // OkHttp's Authenticator is blocking, so runBlocking is acceptable here (it runs on
            // OkHttp's own thread, not the main thread).
            val refreshed = runBlocking { oidc.refresh(user) }

            return refreshed.fold(
                onSuccess = { newUser ->
                    session.update(newUser)
                    withToken(response.request, newUser.token)
                },
                onFailure = { error ->
                    // Refresh token rejected = the session is over. A network error just fails this request.
                    if (error is RefreshRejectedException) session.signOut()
                    null
                }
            )
        }
    }

    private fun withToken(request: Request, token: String): Request =
        request.newBuilder().header("Authorization", "Bearer $token").build()

    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }
}


