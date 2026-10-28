package com.pnc.jetpackcomposedemos

import android.app.Application
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pnc.jetpackcomposedemos.core.EventPlannerApp
import com.pnc.jetpackcomposedemos.core.settings.LocalAppSettings
import com.pnc.jetpackcomposedemos.core.settings.SettingsViewModel
import com.pnc.jetpackcomposedemos.core.settings.ThemeMode
import com.pnc.jetpackcomposedemos.features.auth.data.oidc.AuthorizeUrl
import com.pnc.jetpackcomposedemos.features.auth.data.oidc.OidcRedirects
import com.pnc.jetpackcomposedemos.features.auth.data.oidc.Pkce
import com.pnc.jetpackcomposedemos.ui.theme.KazooTheme
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class EventPlannerApplication: Application()

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var oidcRedirects: OidcRedirects

    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val uri = intent.dataString
        if (uri != null && uri.startsWith(BuildConfig.OIDC_REDIRECT_URI)) {
            oidcRedirects.deliver(uri)
        }
    }

    private companion object {
        val LIGHT_SCRIM = android.graphics.Color.argb(0xe6, 0xFF, 0xFF, 0xFF)
        val DARK_SCRIM = android.graphics.Color.argb(0x80, 0x1b, 0x1b, 0x1b)
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {

            val settings by settingsViewModel.settings.collectAsStateWithLifecycle()
            val darkTheme = when (settings.themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            // enableEdgeToEdge() picked the status and navigation bar icon colors
            // from the SYSTEM theme when the activity was created. Now that the app's
            // own setting can disagree with the system, set them again whenever
            // the chosen theme changes.
            DisposableEffect(darkTheme) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(
                        lightScrim = android.graphics.Color.TRANSPARENT,
                        darkScrim = android.graphics.Color.TRANSPARENT
                    ) {
                        darkTheme
                    },
                    navigationBarStyle = SystemBarStyle.auto(
                        lightScrim = LIGHT_SCRIM,
                        darkScrim = DARK_SCRIM
                    ) {
                        darkTheme
                    }
                )
                onDispose { }
            }

            CompositionLocalProvider(LocalAppSettings provides settings) {
                KazooTheme(darkTheme = darkTheme) {
                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->

                        val modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .padding(horizontal = 16.dp)

                        Box(
                            modifier = modifier
                        ) {
                            EventPlannerApp()
                        }
                    }
                }
            }
        }
    }
}



