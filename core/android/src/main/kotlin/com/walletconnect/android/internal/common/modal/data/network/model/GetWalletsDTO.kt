package com.walletconnect.android.internal.common.modal.data.network.model

import com.squareup.moshi.Json
import com.walletconnect.android.internal.common.modal.data.network.model.WalletDTO

internal data class GetWalletsDTO(
    @Json(name = "count")
    val count: Int,
    @Json(name = "data")
    val data: List<WalletDTO>,
)