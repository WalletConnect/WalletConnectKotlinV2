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

    override fun triggerRequest(payload: JsonRpcClientSync<*>, topic: Topic?) {
        val requestJson = serializer.serialize(payload) ?: throw IllegalStateException("LinkMode: Unknown result params")
        if (jsonRpcHistory.setRequest(payload.id, topic ?: Topic(), payload.method, requestJson, TransportType.LINK_MODE)) {
            val encodedRequest = getEncodedRequest(topic, requestJson)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                //TODO: pass App Link from where?
                data = Uri.parse("https://web3modal-laboratory-git-chore-kotlin-assetlinks-walletconnect1.vercel.app/wallet?wc_ev=$encodedRequest&topic=${topic?.value ?: ""}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    override fun triggerResponse(topic: Topic, response: JsonRpcResponse, participants: Participants?, envelopeType: EnvelopeType) {
        val responseJson = serializer.serialize(response) ?: throw Exception("Null")
        val encodedResponse = Base64.encodeToString(chaChaPolyCodec.encrypt(topic, responseJson, envelopeType, participants), Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            //TODO: pass App Link from where?
            data = Uri.parse("https://web3modal-laboratory-git-chore-kotlin-assetlinks-walletconnect1.vercel.app/dapp?wc_ev=$encodedResponse&topic=${topic.value}")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        jsonRpcHistory.updateRequestWithResponse(response.id, responseJson)
        context.startActivity(intent)
    }

    override fun dispatchEnvelope(url: String) {
        //todo: check if all errors are caught on the client level
        val uri = Uri.parse(url)
        val encodedEnvelope = uri.getQueryParameter("wc_ev") ?: throw IllegalStateException("LinkMode: Unknown result params")
        val topic = uri.getQueryParameter("topic")
        val envelope = getEnvelope(topic, encodedEnvelope)
        scope.launch {
            supervisorScope {
                serializer.tryDeserialize<ClientJsonRpc>(envelope)?.let { clientJsonRpc ->
                    if (jsonRpcHistory.setRequest(clientJsonRpc.id, Topic(topic ?: String.Empty), clientJsonRpc.method, envelope, TransportType.LINK_MODE)) {
                        serializer.deserialize(clientJsonRpc.method, envelope)?.let {
                            _clientSyncJsonRpc.emit(WCRequest(Topic(topic ?: String.Empty), clientJsonRpc.id, clientJsonRpc.method, it, transportType = TransportType.LINK_MODE))
                        }
                    }
                } ?: serializer.tryDeserialize<JsonRpcResponse.JsonRpcResult>(envelope)?.let { result ->
                    val serializedResult = serializer.serialize(result) ?: throw IllegalStateException("LinkMode: Unknown result params")
                    val jsonRpcRecord = jsonRpcHistory.updateRequestWithResponse(result.id, serializedResult)
                    if (jsonRpcRecord != null) {
                        println("kobe: check: ${jsonRpcRecord.method}; ${jsonRpcRecord.body}")
                        serializer.deserialize(jsonRpcRecord.method, jsonRpcRecord.body)?.let { params ->
                            _peerResponse.emit(jsonRpcRecord.toWCResponse(JsonRpcResponse.JsonRpcResult(result.id, result = result.result), params))
                        } ?: throw IllegalStateException("LinkMode: Unknown result params")
                    }
                } ?: serializer.tryDeserialize<JsonRpcResponse.JsonRpcError>(envelope)?.let { error ->
                    val serializedResult = serializer.serialize(error) ?: throw IllegalStateException("LinkMode: Unknown result params")
                    val jsonRpcRecord = jsonRpcHistory.updateRequestWithResponse(error.id, serializedResult)
                    if (jsonRpcRecord != null) {
                        serializer.deserialize(jsonRpcRecord.method, jsonRpcRecord.body)?.let { params ->
                            _peerResponse.emit(jsonRpcRecord.toWCResponse(error, params))
                        } ?: throw IllegalStateException("LinkMode: Unknown error params")
                    }
                } ?: throw IllegalStateException("LinkMode: Received unknown object type")
            }
        }
    }

    private fun getEnvelope(topic: String?, encodedEnvelope: String) = if (!topic.isNullOrEmpty()) {
        chaChaPolyCodec.decrypt(Topic(topic), Base64.decode(encodedEnvelope, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING))
    } else {
        String(Base64.decode(encodedEnvelope, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING), Charsets.UTF_8)
    }

    private fun getEncodedRequest(topic: Topic?, requestJson: String): String? = if (topic != null) {
        val encryptedRequest = chaChaPolyCodec.encrypt(topic, requestJson, EnvelopeType.ZERO)
        Base64.encodeToString(encryptedRequest, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
    } else {
        Base64.encodeToString(requestJson.toByteArray(Charsets.UTF_8), Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
    }
}

interface LinkModeJsonRpcInteractorInterface : JsonRpcInteractorInterface {
    fun triggerRequest(payload: JsonRpcClientSync<*>, topic: Topic? = null)
    fun triggerResponse(topic: Topic, response: JsonRpcResponse, participants: Participants? = null, envelopeType: EnvelopeType = EnvelopeType.ZERO)
    fun dispatchEnvelope(url: String)
}