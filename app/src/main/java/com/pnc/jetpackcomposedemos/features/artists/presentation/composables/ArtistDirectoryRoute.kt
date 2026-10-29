package com.pnc.jetpackcomposedemos.features.artists.presentation.composables

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.NavigableListDetailPaneScaffold
import androidx.compose.material3.adaptive.navigation.ThreePaneScaffoldNavigator
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pnc.jetpackcomposedemos.features.artists.presentation.viewmodels.ArtistDirectoryViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalSharedTransitionApi::class)
@Composable
fun ArtistDirectoryRoute(
    onBack: () -> Unit = {},
    viewModel: ArtistDirectoryViewModel = hiltViewModel()
) {
    // get an observable copy of the state inside our viewModel
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LifecycleEventEffect(Lifecycle.Event.ON_START) {
        viewModel.loadArtists()
    }

    // The navigator remembers which pane is showing and which artist is in the detail pane. It also handles
    // the system back gesture: detail -> list on a phone.
    val navigator = rememberListDetailPaneScaffoldNavigator<Int>()
    val scope = rememberCoroutineScope()

    SharedTransitionLayout {
        CompositionLocalProvider(LocalSharedTransitionScope provides this) {
            NavigableListDetailPaneScaffold(
                navigator = navigator,
                listPane = {
                    AnimatedPane {
                        CompositionLocalProvider(LocalPaneAnimatedScope provides this) {
                            ArtistDirectory(
                                state = uiState,
                                // the scaffold decides one pane or two, so the directory
                                // is always a plain list
                                useTwoPaneLayout = false,
                                onBack = onBack,
                                onArtistSelected = { artistId ->
                                    scope.launch {
                                        navigator.navigateTo(
                                            ListDetailPaneScaffoldRole.Detail,
                                            artistId
                                        )
                                    }
                                },
                                onGenreChange = {
                                    viewModel.updateGenreFilter(it)
                                },
                                onLocationChange = {
                                    viewModel.updateLocationFilter(it)
                                },
                                onShowFilterChange = {
                                    viewModel.updateShowFilter(it)
                                }
                            )
                        }
                    }
                },
                detailPane = {
                    AnimatedPane {
                        CompositionLocalProvider(LocalPaneAnimatedScope provides this) {
                            val artistId = navigator.currentDestination?.contentKey
                            if (artistId != null) {
                                ArtistDetails(artistId = artistId)
                            }
                        }
                    }
                }
            )
        }
    }




}