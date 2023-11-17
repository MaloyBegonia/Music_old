package com.maloy.music.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.maloy.music.R

@Composable
fun getI18String ( stringName: String ): String {
    val i18StringName = when (stringName) {
        "Small" -> stringResource(R.string.i18_small)
        "Medium" -> stringResource(R.string.i18_medium)
        "Big" -> stringResource(R.string.i18_big)
        "None" -> stringResource(R.string.i18_none)
        "Light" -> stringResource(R.string.i18_light)
        "Heavy" -> stringResource(R.string.i18_heavy)
        else -> {""}
    }
    return i18StringName

}