package com.pnc.jetpackcomposedemos.features.artists.domain

import kotlinx.coroutines.flow.first
import javax.inject.Inject

class SyncArtistsUseCase @Inject constructor(
    private val repository: ArtistRepository
) {

    /**
     * Refreshes the cache from the server and returns how many artists are new since the last sync.
     * The first fill of an empty cache reports 0: everything is "new" then, and that is not news.
     */
    suspend operator fun invoke(): Int {
        val before = repository.observeArtists().first().map { it.id }.toSet()
        repository.refreshArtists()
        val after = repository.observeArtists().first()

        return if (before.isEmpty()) 0 else after.count { it.id !in before }
    }

}