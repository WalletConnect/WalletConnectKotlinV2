package com.walletconnect.android.internal.common.model.type

import com.walletconnect.android.internal.common.model.SDKError
import com.walletconnect.android.internal.common.model.WCRequest
import com.walletconnect.android.internal.common.model.WCResponse
import kotlinx.coroutines.flow.SharedFlow

interface JsonRpcInteractorInterface {
    val clientSyncJsonRpc: SharedFlow<WCRequest>
    val peerResponse: SharedFlow<WCResponse>
    val internalErrors: SharedFlow<SDKError>
}