package com.maloy.music.ui.screens.builtinplaylist

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.maloy.compose.persist.persistList
import com.maloy.music.Database
import com.maloy.music.LocalPlayerAwareWindowInsets
import com.maloy.music.LocalPlayerServiceBinder
import com.maloy.music.R
import com.maloy.music.enums.BuiltInPlaylist
import com.maloy.music.enums.SongSortBy
import com.maloy.music.enums.SortOrder
import com.maloy.music.models.Song
import com.maloy.music.models.SongWithContentLength
import com.maloy.music.ui.components.LocalMenuState
import com.maloy.music.ui.components.themed.FloatingActionsContainerWithScrollToTop
import com.maloy.music.ui.components.themed.Header
import com.maloy.music.ui.components.themed.HeaderIconButton
import com.maloy.music.ui.components.themed.HeaderInfo
import com.maloy.music.ui.components.themed.InHistoryMediaItemMenu
import com.maloy.music.ui.components.themed.NonQueuedMediaItemMenu
import com.maloy.music.ui.components.themed.SecondaryButton
import com.maloy.music.ui.items.SongItem
import com.maloy.music.ui.styling.Dimensions
import com.maloy.music.ui.styling.LocalAppearance
import com.maloy.music.ui.styling.px
import com.maloy.music.utils.asMediaItem
import com.maloy.music.utils.enqueue
import com.maloy.music.utils.forcePlayAtIndex
import com.maloy.music.utils.forcePlayFromBeginning
import com.maloy.music.utils.rememberPreference
import com.maloy.music.utils.songSortByKey
import com.maloy.music.utils.songSortOrderKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

@ExperimentalFoundationApi
@ExperimentalAnimationApi
@Composable
fun BuiltInPlaylistSongs(builtInPlaylist: BuiltInPlaylist) {
    val (colorPalette) = LocalAppearance.current
    val binder = LocalPlayerServiceBinder.current
    val menuState = LocalMenuState.current

    var songs by persistList<Song>("${builtInPlaylist.name}/songs")

    var sortBy by rememberPreference(songSortByKey, SongSortBy.DateAdded)
    var sortOrder by rememberPreference(songSortOrderKey, SortOrder.Descending)


    LaunchedEffect(Unit, sortBy, sortOrder) {
        when (builtInPlaylist) {
            BuiltInPlaylist.Favorites -> com.maloy.music.Database
                .songsFavorites(sortBy, sortOrder)

            BuiltInPlaylist.Offline -> com.maloy.music.Database
                .songsOffline(sortBy, sortOrder)
                .flowOn(Dispatchers.IO)
                .map { songs ->
                    songs.filter { song ->
                        song.contentLength?.let {
                            binder?.cache?.isCached(song.song.id, 0, song.contentLength)
                        } ?: false
                    }.map(SongWithContentLength::song)
                }
        }.collect { songs = it }


    }

    val thumbnailSizeDp = Dimensions.thumbnails.song
    val thumbnailSize = thumbnailSizeDp.px

    val sortOrderIconRotation by animateFloatAsState(
        targetValue = if (sortOrder == SortOrder.Ascending) 0f else 180f,
        animationSpec = tween(durationMillis = 400, easing = LinearEasing)
    )

    val lazyListState = rememberLazyListState()



    Box {
        LazyColumn(
            state = lazyListState,
            contentPadding = LocalPlayerAwareWindowInsets.current
                .only(WindowInsetsSides.Vertical + WindowInsetsSides.End).asPaddingValues(),
            modifier = Modifier
                .background(colorPalette.background0)
                .fillMaxSize()
        ) {
            item(
                key = "header",
                contentType = 0
            ) {
                Header(
                    title = when (builtInPlaylist) {
                        BuiltInPlaylist.Favorites -> stringResource(R.string.favorites)
                        BuiltInPlaylist.Offline -> stringResource(R.string.offline)
                    },
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                ) {
                    HeaderInfo(
                        title = "${songs.size}",
                        icon = painterResource(R.drawable.musical_notes),
                        spacer = 0
                    )
                    SecondaryButton(
                        iconId = R.drawable.addqueue,
                        enabled = songs.isNotEmpty(),
                        onClick = {
                            binder?.player?.enqueue(songs.map(Song::asMediaItem))
                        }
                    )

                    Spacer(
                        modifier = Modifier
                            .weight(1f)
                    )

                            HeaderIconButton(
                                icon = R.drawable.trending,
                                color = if (sortBy == SongSortBy.PlayTime) colorPalette.text else colorPalette.textDisabled,
                                onClick = { sortBy = SongSortBy.PlayTime }
                            )

                            HeaderIconButton(
                                icon = R.drawable.text,
                                color = if (sortBy == SongSortBy.Title) colorPalette.text else colorPalette.textDisabled,
                                onClick = { sortBy = SongSortBy.Title }
                            )

                            HeaderIconButton(
                                icon = R.drawable.time,
                                color = if (sortBy == SongSortBy.DateAdded) colorPalette.text else colorPalette.textDisabled,
                                onClick = { sortBy = SongSortBy.DateAdded }
                            )

                            Spacer(
                                modifier = Modifier
                                    .width(2.dp)
                            )

                            HeaderIconButton(
                                icon = R.drawable.arrow_up,
                                color = colorPalette.text,
                                onClick = { sortOrder = !sortOrder },
                                modifier = Modifier
                                    .graphicsLayer { rotationZ = sortOrderIconRotation }
                            )

                }
            }

            itemsIndexed(
                items = songs,
                key = { _, song -> song.id },
                contentType = { _, song -> song },
            ) { index, song ->
                SongItem(
                    song = song,
                    thumbnailSizeDp = thumbnailSizeDp,
                    thumbnailSizePx = thumbnailSize,
                    modifier = Modifier
                        .combinedClickable(
                            onLongClick = {
                                menuState.display {
                                    when (builtInPlaylist) {
                                        BuiltInPlaylist.Favorites -> NonQueuedMediaItemMenu(
                                            mediaItem = song.asMediaItem,
                                            onDismiss = menuState::hide
                                        )

                                        BuiltInPlaylist.Offline -> InHistoryMediaItemMenu(
                                            song = song,
                                            onDismiss = menuState::hide
                                        )
                                    }
                                }
                            },
                            onClick = {
                                binder?.stopRadio()
                                binder?.player?.forcePlayAtIndex(
                                    songs.map(Song::asMediaItem),
                                    index
                                )
                            }
                        )
                        .animateItemPlacement()
                )
            }
        }

        FloatingActionsContainerWithScrollToTop(
            lazyListState = lazyListState,
            iconId = R.drawable.shuffle,
            onClick = {
                if (songs.isNotEmpty()) {
                    binder?.stopRadio()
                    binder?.player?.forcePlayFromBeginning(
                        songs.shuffled().map(Song::asMediaItem)
                    )
                }
            }
        )
    }
}
