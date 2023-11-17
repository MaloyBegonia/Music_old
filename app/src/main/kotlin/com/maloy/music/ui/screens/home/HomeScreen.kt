package com.maloy.music.ui.screens.home

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.maloy.compose.persist.PersistMapCleanup
import com.maloy.compose.routing.RouteHandler
import com.maloy.compose.routing.defaultStacking
import com.maloy.compose.routing.defaultStill
import com.maloy.compose.routing.defaultUnstacking
import com.maloy.compose.routing.isStacking
import com.maloy.compose.routing.isUnknown
import com.maloy.compose.routing.isUnstacking
import com.maloy.music.Database
import com.maloy.music.R
import com.maloy.music.models.SearchQuery
import com.maloy.music.query
import com.maloy.music.ui.components.themed.Scaffold
import com.maloy.music.ui.screens.albumRoute
import com.maloy.music.ui.screens.artistRoute
import com.maloy.music.ui.screens.builtInPlaylistRoute
import com.maloy.music.ui.screens.builtinplaylist.BuiltInPlaylistScreen
import com.maloy.music.ui.screens.globalRoutes
import com.maloy.music.ui.screens.localPlaylistRoute
import com.maloy.music.ui.screens.localplaylist.LocalPlaylistScreen
import com.maloy.music.ui.screens.playlistRoute
import com.maloy.music.ui.screens.search.SearchScreen
import com.maloy.music.ui.screens.searchResultRoute
import com.maloy.music.ui.screens.searchRoute
import com.maloy.music.ui.screens.searchresult.SearchResultScreen
import com.maloy.music.ui.screens.settings.SettingsScreen
import com.maloy.music.ui.screens.settingsRoute
import com.maloy.music.utils.homeScreenTabIndexKey
import com.maloy.music.utils.pauseSearchHistoryKey
import com.maloy.music.utils.preferences
import com.maloy.music.utils.rememberPreference

@ExperimentalFoundationApi
@ExperimentalAnimationApi
@Composable
fun HomeScreen(onPlaylistUrl: (String) -> Unit) {
    val saveableStateHolder = rememberSaveableStateHolder()

    PersistMapCleanup("home/")

    RouteHandler(
        listenToGlobalEmitter = true,
        transitionSpec = {
            when {
                isStacking -> defaultStacking
                isUnstacking -> defaultUnstacking
                isUnknown -> when {
                    initialState.route == searchRoute && targetState.route == searchResultRoute -> defaultStacking
                    initialState.route == searchResultRoute && targetState.route == searchRoute -> defaultUnstacking
                    else -> defaultStill
                }

                else -> defaultStill
            }
        }
    ) {
        globalRoutes()

        settingsRoute {
            SettingsScreen()
        }

        localPlaylistRoute { playlistId ->
            LocalPlaylistScreen(
                playlistId = playlistId ?: error("playlistId cannot be null")
            )
        }

        builtInPlaylistRoute { builtInPlaylist ->
            BuiltInPlaylistScreen(
                builtInPlaylist = builtInPlaylist
            )
        }

        searchResultRoute { query ->
            SearchResultScreen(
                query = query,
                onSearchAgain = {
                    searchRoute(query)
                }
            )
        }

        searchRoute { initialTextInput ->
            val context = LocalContext.current

            SearchScreen(
                initialTextInput = initialTextInput,
                onSearch = { query ->
                    pop()
                    searchResultRoute(query)

                    if (!context.preferences.getBoolean(pauseSearchHistoryKey, false)) {
                        com.maloy.music.query {
                            com.maloy.music.Database.insert(SearchQuery(query = query))
                        }
                    }
                },
                onViewPlaylist = onPlaylistUrl
            )
        }

        host {
            val (tabIndex, onTabChanged) = rememberPreference(
                homeScreenTabIndexKey,
                defaultValue = 0
            )

            Scaffold(
                topIconButtonId = R.drawable.equalizer,
                onTopIconButtonClick = { settingsRoute() },
                //onTopIconButtonClick = { searchRoute("") },
                tabIndex = tabIndex,
                onTabChanged = onTabChanged,
                tabColumnContent = { Item ->
                    Item(0, stringResource(R.string.quick_picks), R.drawable.sparkles)
                    Item(1, stringResource(R.string.songs), R.drawable.musical_notes)
                    Item(2, stringResource(R.string.playlists), R.drawable.playlist)
                    Item(3, stringResource(R.string.artists), R.drawable.person)
                    Item(4, stringResource(R.string.albums), R.drawable.disc)
                    //Item(5, "Statistics", R.drawable.query_stats)
                    //Item(6, "Settings", R.drawable.equalizer)
                }
            ) { currentTabIndex ->
                saveableStateHolder.SaveableStateProvider(key = currentTabIndex) {
                    when (currentTabIndex) {
                        0 -> QuickPicks(
                            onAlbumClick = { albumRoute(it) },
                            onArtistClick = { artistRoute(it) },
                            onPlaylistClick = { playlistRoute(it) },
                            onSearchClick = { searchRoute("") }
                        )

                        1 -> HomeSongs(
                            onSearchClick = { searchRoute("") }
                        )

                        2 -> HomePlaylists(
                            onBuiltInPlaylist = { builtInPlaylistRoute(it) },
                            onPlaylistClick = { localPlaylistRoute(it.id) },
                            onSearchClick = { searchRoute("") }
                        )

                        3 -> HomeArtistList(
                            onArtistClick = { artistRoute(it.id) },
                            onSearchClick = { searchRoute("") }
                        )

                        4 -> HomeAlbums(
                            onAlbumClick = { albumRoute(it.id) },
                            onSearchClick = { searchRoute("") }
                        )
/*
                        5 -> HomeStatistics(
                            onStatisticsType = { statisticsTypeRoute(it)},
                            onBuiltInPlaylist = { builtInPlaylistRoute(it) },
                            onPlaylistClick = { localPlaylistRoute(it.id) },
                            onSearchClick = { searchRoute("") }
                        )
*/
                        //6 -> settingsRoute()
                    }
                }
            }
        }
    }
}
