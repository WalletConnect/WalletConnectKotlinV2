package com.walletconnect.web3.modal.utils

import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import coil.request.ImageRequest
import com.walletconnect.android.BuildConfig

internal fun ImageRequest.Builder.imageHeaders() = apply {
    addHeader("x-project-id", BuildConfig.PROJECT_ID)
    addHeader("x-sdk-version", BuildConfig.SDK_VERSION)
    addHeader("x-sdk-type", "w3m")
}

internal val grayColorFilter = ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })
