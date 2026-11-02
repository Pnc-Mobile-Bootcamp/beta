package com.pnc.jetpackcomposedemos.core.settings


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pnc.jetpackcomposedemos.core.sync.ArtistSyncScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: SettingsRepository,
    private val syncScheduler: ArtistSyncScheduler
) : ViewModel() {

    // Turns the cold DataStore Flow into state the UI can read. The defaults show until the file is read.
    val settings: StateFlow<AppSettings> = repository.settings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AppSettings()
    )

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { repository.setThemeMode(mode) }
    }

    fun setShowArtistLocation(show: Boolean) {
        viewModelScope.launch { repository.setShowArtistLocation(show) }
    }

    /** Remembers the choice AND starts or stops the hourly sync that sends the notifications. */
    fun setNotifyNewArtists(enabled: Boolean) {
        viewModelScope.launch { repository.setNotifyNewArtists(enabled) }
        if (enabled) syncScheduler.schedule() else syncScheduler.cancel()
    }

    fun syncNow() = syncScheduler.syncNow()

}


