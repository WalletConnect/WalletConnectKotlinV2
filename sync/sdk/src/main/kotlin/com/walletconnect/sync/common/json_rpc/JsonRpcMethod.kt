package com.walletconnect.sync.common.json_rpc

internal object JsonRpcMethod {
    @get:JvmSynthetic
    const val WC_SYNC_SET: String = "wc_syncSet"
    @get:JvmSynthetic
    const val WC_SYNC_DELETE: String = "wc_syncDel"
}