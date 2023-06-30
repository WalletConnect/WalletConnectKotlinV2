package com.walletconnect.android.test.json_rpc

import com.squareup.moshi.JsonClass
import com.walletconnect.android.internal.common.model.type.ClientParams

@JsonClass(generateAdapter = true)
class TestHistoryParams : ClientParams