package com.maloy.music.ui.screens.settings

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import com.maloy.music.BuildConfig
import com.maloy.music.LocalPlayerAwareWindowInsets
import com.maloy.music.R
import com.maloy.music.ui.components.themed.Header
import com.maloy.music.ui.styling.LocalAppearance
import com.maloy.music.utils.secondary



@ExperimentalAnimationApi
@Composable
fun About() {
    val (colorPalette, typography) = LocalAppearance.current
    val uriHandler = LocalUriHandler.current


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
        Header(title = stringResource(R.string.about)) {
            BasicText(
                text =  stringResource(R.string.aboutdev),
                style = typography.s.secondary
            )
        }

        SettingsEntryGroupText(title = stringResource(R.string.social))

        SettingsEntry(
            title = stringResource(R.string.telegramchannel),
            text = stringResource(R.string.telegram),
            onClick = {
                uriHandler.openUri("https://t.me/appmusicupdates")
            }
        )


        SettingsEntry(
            title = stringResource(R.string.donate2),
            text = stringResource(R.string.donate),
            onClick = {
                uriHandler.openUri("https://qiwi.com/n/FLOKISOFTPRIVAT")
            }
        )

        SettingsEntry(
            title = stringResource(R.string.git),
            text = stringResource(R.string.view_the_source_code),
            onClick = {
                uriHandler.openUri("https://github.com/MaloyBegonia/music")
            }
        )

        SettingsGroupSpacer()

        SettingsEntryGroupText(title = stringResource(R.string.troubleshooting))

        SettingsEntry(
            title = stringResource(R.string.report_an_issue),
            text = stringResource(R.string.you_will_be_redirected_to_github),
            onClick = {
                uriHandler.openUri("https://github.com/MaloyBegonia/Music/issues/new?assignees=&labels=bug&template=bug_report.yaml")
            }
        )


        SettingsEntry(
            title = stringResource(R.string.request_a_feature_or_suggest_an_idea),
            text = stringResource(R.string.you_will_be_redirected_to_github),
            onClick = {
                uriHandler.openUri("https://github.com/MaloyBegonia/Music/issues/new?assignees=&labels=feature_request&template=feature_request.yaml")
            }
        )
    }
}
