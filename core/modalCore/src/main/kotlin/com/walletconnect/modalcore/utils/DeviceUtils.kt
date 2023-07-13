package com.walletconnect.modalcore.utils

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration

val isLandscape: Boolean
    @Composable
    get() = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE