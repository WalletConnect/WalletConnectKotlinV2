package com.walletconnect.android.internal.common.json_rpc.domain.link_mode

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Base64
import com.walletconnect.android.internal.common.JsonRpcResponse
import com.walletconnect.android.internal.common.crypto.codec.Codec
import com.walletconnect.android.internal.common.json_rpc.data.JsonRpcSerializer
import com.walletconnect.android.internal.common.json_rpc.model.toWCResponse
import com.walletconnect.android.internal.common.model.EnvelopeType
import com.walletconnect.android.internal.common.model.Participants
import com.walletconnect.android.internal.common.model.SDKError
import com.walletconnect.android.internal.common.model.TransportType
import com.walletconnect.android.internal.common.model.WCRequest
import com.walletconnect.android.internal.common.model.WCResponse
import com.walletconnect.android.internal.common.model.sync.ClientJsonRpc
import com.walletconnect.android.internal.common.model.type.JsonRpcClientSync
import com.walletconnect.android.internal.common.model.type.JsonRpcInteractorInterface
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.internal.common.storage.rpc.JsonRpcHistory
import com.walletconnect.android.internal.common.wcKoinApp
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.util.Empty
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

class LinkModeJsonRpcInteractor(
    private val chaChaPolyCodec: Codec,
    private val jsonRpcHistory: JsonRpcHistory,
    private val context: Context,
) : LinkModeJsonRpcInteractorInterface {
    private val serializer: JsonRpcSerializer get() = wcKoinApp.koin.get()

    private val _clientSyncJsonRpc: MutableSharedFlow<WCRequest> = MutableSharedFlow()
    override val clientSyncJsonRpc: SharedFlow<WCRequest> = _clientSyncJsonRpc.asSharedFlow()

    private val _peerResponse: MutableSharedFlow<WCResponse> = MutableSharedFlow()
    override val peerResponse: SharedFlow<WCResponse> = _peerResponse.asSharedFlow()

    private val _internalErrors = MutableSharedFlow<SDKError>()
    override val internalErrors: SharedFlow<SDKError> = _internalErrors.asSharedFlow()

    override fun triggerRequest(payload: JsonRpcClientSync<*>, topic: Topic, appLink: String, envelopeType: EnvelopeType) {
        val requestJson = serializer.serialize(payload) ?: throw IllegalStateException("LinkMode: Cannot serialize the request")
        if (jsonRpcHistory.setRequest(payload.id, topic, payload.method, requestJson, TransportType.LINK_MODE)) {
            val encryptedResponse = chaChaPolyCodec.encrypt(topic, requestJson, envelopeType)
            val encodedRequest = Base64.encodeToString(encryptedResponse, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("$appLink?wc_ev=$encodedRequest&topic=${topic.value}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    override fun triggerResponse(topic: Topic, response: JsonRpcResponse, appLink: String, participants: Participants?, envelopeType: EnvelopeType) {
        val responseJson = serializer.serialize(response) ?: throw IllegalStateException("LinkMode: Cannot serialize the response")
        val encryptedResponse = chaChaPolyCodec.encrypt(topic, responseJson, envelopeType, participants)
        val encodedResponse = Base64.encodeToString(encryptedResponse, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("$appLink?wc_ev=$encodedResponse&topic=${topic.value}")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        jsonRpcHistory.updateRequestWithResponse(response.id, responseJson)
        context.startActivity(intent)
    }

    override fun dispatchEnvelope(url: String) {
        val uri = Uri.parse(url)
        val encodedEnvelope = uri.getQueryParameter("wc_ev") ?: throw IllegalStateException("LinkMode: Missing wc_ev parameter")
        val topic = uri.getQueryParameter("topic") ?: throw IllegalStateException("LinkMode: Missing topic parameter")
        val decoded = Base64.decode(encodedEnvelope, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
        val envelope = chaChaPolyCodec.decrypt(Topic(topic), decoded)

        scope.launch {
            supervisorScope {
                serializer.tryDeserialize<ClientJsonRpc>(envelope)?.let { clientJsonRpc -> serializeRequest(clientJsonRpc, topic, envelope) }
                    ?: serializer.tryDeserialize<JsonRpcResponse.JsonRpcResult>(envelope)?.let { result -> serializeResult(result) }
                    ?: serializer.tryDeserialize<JsonRpcResponse.JsonRpcError>(envelope)?.let { error -> serializeError(error) }
                    ?: throw IllegalStateException("LinkMode: Received unknown object type")
            }
        }
    }

    private suspend fun serializeRequest(clientJsonRpc: ClientJsonRpc, topic: String?, envelope: String) {
        if (jsonRpcHistory.setRequest(clientJsonRpc.id, Topic(topic ?: String.Empty), clientJsonRpc.method, envelope, TransportType.LINK_MODE)) {
            serializer.deserialize(clientJsonRpc.method, envelope)?.let {
                _clientSyncJsonRpc.emit(WCRequest(Topic(topic ?: String.Empty), clientJsonRpc.id, clientJsonRpc.method, it, transportType = TransportType.LINK_MODE))
            }
        }
    }

    private suspend fun serializeResult(result: JsonRpcResponse.JsonRpcResult) {
        val serializedResult = serializer.serialize(result) ?: throw IllegalStateException("LinkMode: Unknown result params")
        val jsonRpcRecord = jsonRpcHistory.updateRequestWithResponse(result.id, serializedResult)
        if (jsonRpcRecord != null) {
            serializer.deserialize(jsonRpcRecord.method, jsonRpcRecord.body)?.let { params ->
                _peerResponse.emit(jsonRpcRecord.toWCResponse(JsonRpcResponse.JsonRpcResult(result.id, result = result.result), params))
            } ?: throw IllegalStateException("LinkMode: Cannot serialize result")
        }
    }

    private suspend fun serializeError(error: JsonRpcResponse.JsonRpcError) {
        val serializedResult = serializer.serialize(error) ?: throw IllegalStateException("LinkMode: Unknown result params")
        val jsonRpcRecord = jsonRpcHistory.updateRequestWithResponse(error.id, serializedResult)
        if (jsonRpcRecord != null) {
            serializer.deserialize(jsonRpcRecord.method, jsonRpcRecord.body)?.let { params ->
                _peerResponse.emit(jsonRpcRecord.toWCResponse(error, params))
            } ?: throw IllegalStateException("LinkMode: Cannot serialize error")
        }
    }
}

interface LinkModeJsonRpcInteractorInterface : JsonRpcInteractorInterface {
    fun triggerRequest(payload: JsonRpcClientSync<*>, topic: Topic, appLink: String, envelopeType: EnvelopeType = EnvelopeType.ZERO)
    fun triggerResponse(topic: Topic, response: JsonRpcResponse, appLink: String, participants: Participants? = null, envelopeType: EnvelopeType = EnvelopeType.ZERO)
    fun dispatchEnvelope(url: String)
}