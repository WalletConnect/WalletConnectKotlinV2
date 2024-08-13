@file:JvmSynthetic

package com.walletconnect.android.internal.common.json_rpc.model

import com.walletconnect.android.internal.common.JsonRpcResponse
import com.walletconnect.android.internal.common.json_rpc.domain.relay.Subscription
import com.walletconnect.android.internal.common.model.IrnParams
import com.walletconnect.android.internal.common.model.TransportType
import com.walletconnect.android.internal.common.model.WCRequest
import com.walletconnect.android.internal.common.model.WCResponse
import com.walletconnect.android.internal.common.model.sync.ClientJsonRpc
import com.walletconnect.android.internal.common.model.type.ClientParams
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.foundation.network.model.Relay

@JvmSynthetic
internal fun JsonRpcHistoryRecord.toWCResponse(result: JsonRpcResponse, params: ClientParams): WCResponse =
    WCResponse(Topic(topic), method, result, params)

@JvmSynthetic
internal fun IrnParams.toRelay(): Relay.Model.IrnParams =
    Relay.Model.IrnParams(tag.id, ttl.seconds, prompt)

internal fun Subscription.toWCRequest(clientJsonRpc: ClientJsonRpc, params: ClientParams, transportType: TransportType): WCRequest =
    WCRequest(topic, clientJsonRpc.id, clientJsonRpc.method, params, decryptedMessage, publishedAt, encryptedMessage, attestation, transportType)