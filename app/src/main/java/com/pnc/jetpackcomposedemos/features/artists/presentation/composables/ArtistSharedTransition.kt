package com.pnc.jetpackcomposedemos.features.artists.presentation.composables

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier

// The two scopes a shared element needs. Passing them through ArtistDirectory
// -> ArtistList -> ArtistCard
// would change every signature, so they travel as CompositionLocals instead.
// Null means "no animation".
@OptIn(ExperimentalSharedTransitionApi::class)
val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope?> { null }
val LocalPaneAnimatedScope = compositionLocalOf<AnimatedVisibilityScope?> { null }


/**
 * Marks an image as the same element on the list card and on the detail pane, so it animates between them.
 * Outside a SharedTransitionLayout (previews, UI tests) it does nothing.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun Modifier.artistImageSharedElement(artistId: Int): Modifier {
    val sharedScope = LocalSharedTransitionScope.current ?: return this
    val animatedScope = LocalPaneAnimatedScope.current ?: return this
    return with(sharedScope) {
        this@artistImageSharedElement.sharedElement(
            sharedContentState = rememberSharedContentState(key = "artist-image-$artistId"),
            animatedVisibilityScope = animatedScope
        )
    }
}



