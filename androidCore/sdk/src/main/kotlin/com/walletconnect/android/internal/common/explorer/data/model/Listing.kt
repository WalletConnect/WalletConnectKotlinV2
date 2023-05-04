package com.walletconnect.android.internal.common.explorer.data.model

data class Listing(
    val id: String,
    val name: String,
    val description: String?,
    val homepage: String,
    val chains: List<String>,
    val versions: List<String>,
    val sdks: List<String>,
    val appType: String,
    val imageId: String,
    val imageUrl: ImageUrl,
    val app: App,
    val injected: String?,
    val mobile: Mobile,
    val desktop: Desktop,
    val supportedStandards: List<SupportedStandard>,
    val metadata: Metadata,
    val updatedAt: String
)