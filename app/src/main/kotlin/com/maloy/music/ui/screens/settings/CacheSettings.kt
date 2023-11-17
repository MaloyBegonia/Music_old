package com.maloy.music.ui.screens.settings

import android.content.Intent
import android.os.Build
import android.text.format.Formatter
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import coil.Coil
import coil.annotation.ExperimentalCoilApi
import com.maloy.music.LocalPlayerAwareWindowInsets
import com.maloy.music.LocalPlayerServiceBinder
import com.maloy.music.R
import com.maloy.music.enums.CoilDiskCacheMaxSize
import com.maloy.music.enums.ExoPlayerDiskCacheMaxSize
import com.maloy.music.ui.components.themed.Header
import com.maloy.music.ui.styling.LocalAppearance
import com.maloy.music.utils.coilDiskCacheMaxSizeKey
import com.maloy.music.utils.exoPlayerAlternateCacheLocationKey
import com.maloy.music.utils.exoPlayerDiskCacheMaxSizeKey
import com.maloy.music.utils.rememberPreference

@OptIn(ExperimentalCoilApi::class)
@ExperimentalAnimationApi
@Composable
fun CacheSettings() {
    val context = LocalContext.current
    val (colorPalette) = LocalAppearance.current
    val binder = LocalPlayerServiceBinder.current

    var coilDiskCacheMaxSize by rememberPreference(
        coilDiskCacheMaxSizeKey,
        CoilDiskCacheMaxSize.`128MB`
    )
    var exoPlayerDiskCacheMaxSize by rememberPreference(
        exoPlayerDiskCacheMaxSizeKey,
        ExoPlayerDiskCacheMaxSize.`2GB`
    )

    var exoPlayerAlternateCacheLocation by rememberPreference(
        exoPlayerAlternateCacheLocationKey,""
    )

    val dirRequest = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        uri?.let {
            context.applicationContext.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            context.applicationContext.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            Log.d("exoAltLocationCache",uri.path.toString())
            exoPlayerAlternateCacheLocation = uri.path.toString()
        }
    }

    //val release = Build.VERSION.RELEASE;
    val sdkVersion = Build.VERSION.SDK_INT;
    //if (sdkVersion.toShort() < 29) exoPlayerAlternateCacheLocation=""
    //Log.d("SystemInfo","Android SDK: " + sdkVersion + " (" + release +")")

    Column(
        modifier = Modifier
            .background(colorPalette.background0)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(
                LocalPlayerAwareWindowInsets.current
                    .only(WindowInsetsSides.Vertical + WindowInsetsSides.End)
                    .asPaddingValues()
            )
    ) {
        Header(title = stringResource(R.string.cache))

        SettingsDescription(text = stringResource(R.string.cache_cleared))

        Coil.imageLoader(context).diskCache?.let { diskCache ->
            val diskCacheSize = remember(diskCache) {
                diskCache.size
            }

            SettingsGroupSpacer()

            SettingsEntryGroupText(title = stringResource(R.string.image_cache))

            SettingsDescription(
                text = "${
                    Formatter.formatShortFileSize(
                        context,
                        diskCacheSize
                    )
                } ${stringResource(R.string.used)} (${diskCacheSize * 100 / coilDiskCacheMaxSize.bytes.coerceAtLeast(1)}%)"
            )

            EnumValueSelectorSettingsEntry(
                title = stringResource(R.string.max_size),
                selectedValue = coilDiskCacheMaxSize,
                onValueSelected = { coilDiskCacheMaxSize = it }
            )
        }

        binder?.cache?.let { cache ->
            val diskCacheSize by remember {
                derivedStateOf {
                    cache.cacheSpace
                }
            }

            SettingsGroupSpacer()

            SettingsEntryGroupText(title = stringResource(R.string.song_cache))

            SettingsDescription(
                text = buildString {
                    append(Formatter.formatShortFileSize(context, diskCacheSize))
                    append(stringResource(R.string.used))
                    when (val size = exoPlayerDiskCacheMaxSize) {
                        ExoPlayerDiskCacheMaxSize.Unlimited -> {}
                        else -> append(" (${diskCacheSize * 100 / size.bytes}%)")
                    }
                }
            )

            EnumValueSelectorSettingsEntry(
                title = stringResource(R.string.max_size),
                selectedValue = exoPlayerDiskCacheMaxSize,
                onValueSelected = { exoPlayerDiskCacheMaxSize = it }
            )
        }

        SettingsGroupSpacer()

        SettingsEntryGroupText(title = stringResource(R.string.folder_cache))

        SettingsDescription(
            text = stringResource(R.string.custom_cache_from_android_10_may_not_be_available)
        )

        SettingsEntry(
            title = stringResource(R.string.cache_location_folder),
            text = if (exoPlayerAlternateCacheLocation == "") stringResource(R.string.defaultfoldername) else exoPlayerAlternateCacheLocation,
            isEnabled = if (sdkVersion.toShort() < 29) true else false,
            onClick = {
                dirRequest.launch(null)
            }
        )

        ImportantSettingsDescription(text = stringResource(R.string.cache_reset_by_clicking_button))

        SettingsEntry(
            title = stringResource(R.string.reset_cache_location_folder),
            text = "",
            isEnabled = if (sdkVersion.toShort() < 29) true else false,
            onClick = {
                exoPlayerAlternateCacheLocation = ""
            }
        )


    }
}
