package com.pnc.jetpackcomposedemos.features.auth.domain

import com.pnc.jetpackcomposedemos.core.ApplicationScope
import com.pnc.jetpackcomposedemos.features.auth.data.storage.TokenStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor(
    private val store: TokenStore,
    @ApplicationScope private val scope: CoroutineScope
) {

    private val _state = MutableStateFlow<AuthState>(AuthState.Restoring)
    val state: StateFlow<AuthState> = _state.asStateFlow()

    val currentUser: User?
        get() = (_state.value as? AuthState.Authenticated)?.user

    init {
        // Saving is a side effect of the state. One collector handles changes in order, and if the
        // state changes quickly only the latest value matters, so a slow write can't overwrite a newer one.
        scope.launch {
            _state.collect { current ->
                when (current) {
                    is AuthState.Authenticated -> store.save(current.user)
                    AuthState.Unauthenticated -> store.clear()
                    AuthState.Restoring -> Unit
                }
            }
        }
    }

    /** Called once at startup. */
    suspend fun restore() {
        if (_state.value !is AuthState.Restoring) return
        val saved = store.load()
        _state.value = if (saved != null) AuthState.Authenticated(saved) else AuthState.Unauthenticated
    }


    fun signIn(user: User) {
        _state.value = AuthState.Authenticated(user)
    }

    /** Replaces the user after a token refresh. */
    fun update(user: User) {
        _state.value = AuthState.Authenticated(user)
    }

    fun signOut() {
        _state.value = AuthState.Unauthenticated
    }

}