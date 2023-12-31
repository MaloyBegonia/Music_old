package com.maloy.music.ui.screens.player

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
//import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.C
import androidx.media3.common.Player
import com.maloy.music.Database
import com.maloy.music.LocalPlayerServiceBinder
import com.maloy.music.R
import com.maloy.music.models.Song
import com.maloy.music.query
import com.maloy.music.ui.components.SeekBar
import com.maloy.music.ui.components.themed.IconButton
import com.maloy.music.ui.components.themed.ScrollText
import com.maloy.music.ui.screens.albumRoute
import com.maloy.music.ui.screens.artistRoute
import com.maloy.music.ui.styling.LocalAppearance
import com.maloy.music.ui.styling.collapsedPlayerProgressBar
import com.maloy.music.ui.styling.favoritesIcon
import com.maloy.music.utils.bold
import com.maloy.music.utils.forceSeekToNext
import com.maloy.music.utils.forceSeekToPrevious
import com.maloy.music.utils.formatAsDuration
import com.maloy.music.utils.rememberPreference
import com.maloy.music.utils.secondary
import com.maloy.music.utils.semiBold
import com.maloy.music.utils.trackLoopEnabledKey
import kotlinx.coroutines.flow.distinctUntilChanged

/*
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MarqueeEffect(
    text: String
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.CenterStart
    ) {
        BasicText(
            modifier = Modifier.basicMarquee(),
            text = text
        )
    }
}
*/

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Controls(
    mediaId: String,
    title: String?,
    artist: String?,
    artistIds: ArrayList<String>?,
    albumId: String?,
    shouldBePlaying: Boolean,
    position: Long,
    duration: Long,
    modifier: Modifier = Modifier
) {
    val (colorPalette, typography) = LocalAppearance.current

    val binder = LocalPlayerServiceBinder.current
    binder?.player ?: return

    var trackLoopEnabled by rememberPreference(trackLoopEnabledKey, defaultValue = false)

    var scrubbingPosition by remember(mediaId) {
        mutableStateOf<Long?>(null)
    }

    val onGoToArtist = artistRoute::global
    val onGoToAlbum = albumRoute::global


    var likedAt by rememberSaveable {
        mutableStateOf<Long?>(null)
    }


    var nextmediaItemIndex = binder.player.nextMediaItemIndex ?: -1
    var nextmediaItemtitle = ""


    if (nextmediaItemIndex.toShort() > -1)
        nextmediaItemtitle = binder.player.getMediaItemAt(nextmediaItemIndex).mediaMetadata.title.toString()


    LaunchedEffect(mediaId) {
        com.maloy.music.Database.likedAt(mediaId).distinctUntilChanged().collect { likedAt = it }
    }

    val shouldBePlayingTransition = updateTransition(shouldBePlaying, label = "shouldBePlaying")

    val playPauseRoundness by shouldBePlayingTransition.animateDp(
        transitionSpec = { tween(durationMillis = 100, easing = LinearEasing) },
        label = "playPauseRoundness",
        targetValueByState = { if (it) 32.dp else 16.dp }
    )


    Column(
        horizontalAlignment = Alignment.Start,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp)
    ) {

        BasicText(
            text = stringResource(R.string.now_playing),
            style = typography.xxs.secondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(
            modifier = Modifier
                .height(5.dp)
        )


        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
           IconButton(
                icon = R.drawable.disc,
                color = if (albumId == null) colorPalette.textDisabled else colorPalette.text,
                enabled = if (albumId == null) false else true,
                onClick = {
                        onGoToAlbum(albumId)
                },
                modifier = Modifier
                    .size(24.dp)
            )

            Spacer(
                modifier = Modifier
                    .width(8.dp)                    
            )

            ScrollText(
                text = title ?: "",
                style = TextStyle(
                    color = colorPalette.text,
                    fontStyle = typography.l.bold.fontStyle,
                    fontSize = typography.l.fontSize
                ),
                onClick = { onGoToAlbum(albumId) }
            )

        }

        Spacer(
            modifier = Modifier
                .height(10.dp)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {


                artistIds?.distinct()?.forEach {
                        IconButton(
                            icon = R.drawable.person,
                            color = if (it == "") colorPalette.textDisabled else colorPalette.text,
                            enabled = it != "",
                            onClick = {
                                onGoToArtist(it)
                            },
                            modifier = Modifier
                                .size(24.dp)

                        )
                    Spacer(
                        modifier = Modifier
                            .width(6.dp)
                    )
                }

            Spacer(
                modifier = Modifier
                    .width(4.dp)
            )

            ScrollText(
                text = artist ?: "",
                style = TextStyle(
                    color = colorPalette.text,
                    fontStyle = typography.l.bold.fontStyle,
                    fontSize = typography.l.fontSize
                ),
                onClick = { onGoToArtist(artistIds?.get(0).toString()) }
            )

        }

        Spacer(
            modifier = Modifier
                .height(15.dp)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            BasicText(
                text = stringResource(R.string.next_playing),
                style = typography.xxs.secondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(
                modifier = Modifier
                    .height(5.dp)
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(
                icon = R.drawable.playlist,
                color = colorPalette.text,
                enabled = false,
                onClick = {
                    //if add future action
                },
                modifier = Modifier
                    .size(14.dp)
            )
            IconButton(
                icon = R.drawable.chevron_forward,
                color = colorPalette.text,
                enabled = false,
                onClick = {
                    //if add future action
                },
                modifier = Modifier
                    .size(14.dp)
            )

            Spacer(
                modifier = Modifier
                    .width(8.dp)
            )

            BasicText(
                text = AnnotatedString(nextmediaItemtitle.toString() ?: ""),
                style = typography.xs.secondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis

            )

        }

        Spacer(
            modifier = Modifier
                .height(30.dp)
        )


        SeekBar(
            value = scrubbingPosition ?: position,
            minimumValue = 0,
            maximumValue = duration,
            onDragStart = {
                scrubbingPosition = it
            },
            onDrag = { delta ->
                scrubbingPosition = if (duration != C.TIME_UNSET) {
                    scrubbingPosition?.plus(delta)?.coerceIn(0, duration)
                } else {
                    null
                }
            },
            onDragEnd = {
                scrubbingPosition?.let(binder.player::seekTo)
                scrubbingPosition = null
            },
            color = colorPalette.collapsedPlayerProgressBar,
            backgroundColor = colorPalette.textSecondary,
            shape = RoundedCornerShape(8.dp)
        )

        Spacer(
            modifier = Modifier
                .height(8.dp)
        )

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            BasicText(
                text = formatAsDuration(scrubbingPosition ?: position),
                style = typography.xxs.semiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            if (duration != C.TIME_UNSET) {
                BasicText(
                    text = formatAsDuration(duration),
                    style = typography.xxs.semiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        Spacer(
            modifier = Modifier
                .weight(1f)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            IconButton(
                icon = if (likedAt == null) R.drawable.heart_outline else R.drawable.heart,
                color = colorPalette.favoritesIcon,
                onClick = {
                    val currentMediaItem = binder.player.currentMediaItem
                    com.maloy.music.query {
                        if (com.maloy.music.Database.like(
                                mediaId,
                                if (likedAt == null) System.currentTimeMillis() else null
                            ) == 0
                        ) {
                            currentMediaItem
                                ?.takeIf { it.mediaId == mediaId }
                                ?.let {
                                    com.maloy.music.Database.insert(
                                        currentMediaItem,
                                        Song::toggleLike
                                    )
                                }
                        }
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .size(24.dp)
            )

            IconButton(
                icon = R.drawable.play_skip_previous,
                color = colorPalette.iconButtonPlayer,
                onClick = binder.player::forceSeekToPrevious,
                modifier = Modifier
                    .weight(1f)
                    .size(34.dp)
            )

            Spacer(
                modifier = Modifier
                    .width(8.dp)
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(playPauseRoundness))
                    .clickable {
                        if (shouldBePlaying) {
                            binder.player.pause()
                        } else {
                            if (binder.player.playbackState == Player.STATE_IDLE) {
                                binder.player.prepare()
                            }
                            binder.player.play()
                        }
                    }
                    .background(colorPalette.background3)
                    .size(64.dp)
            ) {
                Image(
                    painter = painterResource(if (shouldBePlaying) R.drawable.play_pause else R.drawable.play_arrow),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(colorPalette.iconButtonPlayer),
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(34.dp)
                )
            }

            Spacer(
                modifier = Modifier
                    .width(8.dp)
            )

            IconButton(
                icon = R.drawable.play_skip_next,
                color = colorPalette.iconButtonPlayer,
                onClick = binder.player::forceSeekToNext,
                modifier = Modifier
                    .weight(1f)
                    .size(34.dp)
            )

            IconButton(
                icon = R.drawable.infinite,
                color = if (trackLoopEnabled) colorPalette.iconButtonPlayer else colorPalette.textDisabled,
                onClick = { trackLoopEnabled = !trackLoopEnabled },
                modifier = Modifier
                    .weight(1f)
                    .size(24.dp)
            )
        }

        Spacer(
            modifier = Modifier
                .weight(1f)
        )
    }
}
