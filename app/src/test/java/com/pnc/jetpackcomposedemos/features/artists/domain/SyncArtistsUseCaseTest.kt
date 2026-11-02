package com.pnc.jetpackcomposedemos.features.artists.domain



import com.pnc.jetpackcomposedemos.features.artists.data.FakeArtistRepository
import com.pnc.jetpackcomposedemos.testutil.builders.anArtist
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class SyncArtistsUseCaseTest {

    // A repository whose "server" has [serverArtists]; refreshing copies them into the cache.
    private class CopyingRepository(
        initialCache: List<Artist>,
        private val serverArtists: List<Artist>
    ) : ArtistRepository {
        private val cache = FakeArtistRepository(initialCache)
        override fun observeArtists(): Flow<List<Artist>> = cache.observeArtists()
        override fun observeArtist(id: Int): Flow<Artist?> = cache.observeArtist(id)
        override suspend fun refreshArtists() { cache.artists.value = serverArtists }
        override suspend fun refreshArtist(id: Int) = Unit
    }

    @Test
    fun `an empty cache is filled without reporting anything as new`() = runTest {
        val repository = CopyingRepository(emptyList(), listOf(anArtist(1), anArtist(2)))

        assertEquals(0, SyncArtistsUseCase(repository)())
    }

    @Test
    fun `artists that were not in the cache are counted`() = runTest {
        val repository = CopyingRepository(
            initialCache = listOf(anArtist(1), anArtist(2)),
            serverArtists = listOf(anArtist(1), anArtist(2), anArtist(3), anArtist(4))
        )

        assertEquals(2, SyncArtistsUseCase(repository)())
    }

    @Test
    fun `nothing changed on the server means nothing new`() = runTest {
        val same = listOf(anArtist(1), anArtist(2))
        val repository = CopyingRepository(same, same)

        assertEquals(0, SyncArtistsUseCase(repository)())
    }

    @Test
    fun `an artist the server dropped is not counted as new`() = runTest {
        val repository = CopyingRepository(
            initialCache = listOf(anArtist(1), anArtist(2)),
            serverArtists = listOf(anArtist(1))
        )

        assertEquals(0, SyncArtistsUseCase(repository)())
    }
}



