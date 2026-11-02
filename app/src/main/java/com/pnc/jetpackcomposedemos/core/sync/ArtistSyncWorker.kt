package com.pnc.jetpackcomposedemos.core.sync


import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.pnc.jetpackcomposedemos.features.artists.domain.SyncArtistsUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CancellationException
import timber.log.Timber
import java.io.IOException

@HiltWorker
class ArtistSyncWorker @AssistedInject constructor(
    // @Assisted values come from WorkManager; everything else comes from Hilt.
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val syncArtists: SyncArtistsUseCase,
    private val notifier: ArtistSyncNotifier
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val newArtists = syncArtists()
            if (newArtists > 0) {
                notifier.notifyNewArtists(newArtists)
            }
            Result.success()
        } catch (e: CancellationException) {
            throw e
        } catch (e: IOException) {
            // No network or the server was unreachable: let WorkManager try again later with backoff.
            Timber.w(e, "Artist sync could not reach the server; will retry")
            Result.retry()
        } catch (e: Exception) {
            Timber.e(e, "Artist sync failed")
            Result.failure()
        }
    }
}

