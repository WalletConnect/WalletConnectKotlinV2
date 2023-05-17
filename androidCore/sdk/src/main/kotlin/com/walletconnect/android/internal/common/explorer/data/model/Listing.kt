package com.walletconnect.android.internal.common.explorer.data.model

import android.net.Uri

data class Listing(
    val id: String,
    val name: String,
    val description: String?,
    val homepage: Uri,
    val chains: List<String>,
    val versions: List<String>,
    val sdks: List<String>,
    val appType: String,
    val imageId: String,
    val imageUrl: ImageUrl,
    val app: App,
    val injected: List<Injected>?,
    val mobile: Mobile,
    val desktop: Desktop,
    val supportedStandards: List<SupportedStandard>,
    val metadata: Metadata,
    val updatedAt: String
)