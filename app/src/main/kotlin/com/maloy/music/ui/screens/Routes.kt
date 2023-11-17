package com.maloy.music.ui.screens

import android.annotation.SuppressLint
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.Composable
import com.maloy.compose.routing.Route0
import com.maloy.compose.routing.Route1
import com.maloy.compose.routing.RouteHandlerScope
import com.maloy.music.enums.BuiltInPlaylist
import com.maloy.music.enums.StatisticsType
import com.maloy.music.ui.screens.album.AlbumScreen
import com.maloy.music.ui.screens.artist.ArtistScreen
import com.maloy.music.ui.screens.playlist.PlaylistScreen
import com.maloy.music.ui.screens.statistics.StatisticsScreen

val quickpicksRoute = Route1<String?>("quickpicksRoute")
val albumRoute = Route1<String?>("albumRoute")
val artistRoute = Route1<String?>("artistRoute")
val builtInPlaylistRoute = Route1<BuiltInPlaylist>("builtInPlaylistRoute")
val statisticsTypeRoute = Route1<StatisticsType>("statisticsTypeRoute")
val localPlaylistRoute = Route1<Long?>("localPlaylistRoute")
val playlistRoute = Route1<String?>("playlistRoute")
val searchResultRoute = Route1<String>("searchResultRoute")
val searchRoute = Route1<String>("searchRoute")
val settingsRoute = Route0("settingsRoute")

@SuppressLint("ComposableNaming")
@Suppress("NOTHING_TO_INLINE")
@ExperimentalAnimationApi
@ExperimentalFoundationApi
@Composable
inline fun RouteHandlerScope.globalRoutes() {
    albumRoute { browseId ->
        AlbumScreen(
            browseId = browseId ?: error("browseId cannot be null")
        )
    }

    artistRoute { browseId ->
        ArtistScreen(
            browseId = browseId ?: error("browseId cannot be null")
        )
    }

    playlistRoute { browseId ->
        PlaylistScreen(
            browseId = browseId ?: error("browseId cannot be null")
        )
    }

    statisticsTypeRoute { browseId ->
        StatisticsScreen(
            statisticsType = browseId ?: error("browseId cannot be null")
        )
    }

    quickpicksRoute { browseId ->

    }
}
