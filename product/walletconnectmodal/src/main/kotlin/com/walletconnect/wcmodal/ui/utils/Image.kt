package com.walletconnect.wcmodal.ui.utils

import coil.request.ImageRequest
import com.walletconnect.android.BuildConfig
import com.walletconnect.android.internal.common.model.ProjectId
import com.walletconnect.android.internal.common.wcKoinApp

internal fun ImageRequest.Builder.imageHeaders() = apply {
    addHeader("x-project-id", wcKoinApp.koin.get<ProjectId>().value)
    addHeader("x-sdk-version", BuildConfig.SDK_VERSION)
    addHeader("x-sdk-type", "wcm")
}