package com.pnc.jetpackcomposedemos.core.settings

import androidx.compose.runtime.compositionLocalOf

// NOT static: this value changes while the app runs, and only the composables that read it should recompose.
val LocalAppSettings = compositionLocalOf { AppSettings() }



