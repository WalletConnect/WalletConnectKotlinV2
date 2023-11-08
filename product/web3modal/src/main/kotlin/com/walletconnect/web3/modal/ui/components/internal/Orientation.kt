package com.walletconnect.web3.modal.ui.components.internal

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.platform.LocalConfiguration

@Composable
internal fun OrientationBox(
    portrait: @Composable () -> Unit,
    landscape: @Composable () -> Unit
) {
    val configuration = LocalConfiguration.current
    var orientation by remember { mutableIntStateOf(Configuration.ORIENTATION_PORTRAIT) }

    LaunchedEffect(configuration) {
        snapshotFlow { configuration.orientation }
            .collect { orientation = it }
    }

    // TODO migrate to WindowSize when will you migrate to material3
    when (configuration.orientation) {
        Configuration.ORIENTATION_LANDSCAPE -> landscape()
        else -> portrait()
    }
}