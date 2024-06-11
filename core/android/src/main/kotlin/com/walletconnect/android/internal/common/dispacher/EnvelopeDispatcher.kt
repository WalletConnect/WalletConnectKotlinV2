package com.walletconnect.android.internal.common.dispacher

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
import com.walletconnect.android.internal.common.model.WCRequest
import com.walletconnect.android.internal.common.model.WCResponse
import com.walletconnect.android.internal.common.model.sync.ClientJsonRpc
import com.walletconnect.android.internal.common.model.type.JsonRpcClientSync
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.internal.common.storage.rpc.JsonRpcHistory
import com.walletconnect.android.internal.common.wcKoinApp
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.foundation.util.Logger
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

class EnvelopeDispatcher(
    private val chaChaPolyCodec: Codec,
    private val jsonRpcHistory: JsonRpcHistory,
//    private val serializer: JsonRpcSerializer,
    private val context: Context,
    private val logger: Logger
) : EnvelopeDispatcherInterface {
    private val serializer: JsonRpcSerializer get() = wcKoinApp.koin.get()

    private val _clientSyncJsonRpc: MutableSharedFlow<WCRequest> = MutableSharedFlow()
    override val clientSyncJsonRpc: SharedFlow<WCRequest> = _clientSyncJsonRpc.asSharedFlow()

    private val _peerResponse: MutableSharedFlow<WCResponse> = MutableSharedFlow()
    override val peerResponse: SharedFlow<WCResponse> = _peerResponse.asSharedFlow()

    private val _internalErrors = MutableSharedFlow<SDKError>()
    override val internalErrors: SharedFlow<SDKError> = _internalErrors.asSharedFlow()

    override fun triggerRequest(payload: JsonRpcClientSync<*>) {
        val requestJson = serializer.serialize(payload) ?: throw Exception("Null")

        println("kobe: Request: $requestJson: ${payload.id}")

        try {
            if (jsonRpcHistory.setRequest(payload.id, Topic("topic"), payload.method, requestJson)) {
                val encodedRequest = Base64.encodeToString(requestJson.toByteArray(Charsets.UTF_8), Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)

                val intent = Intent(Intent.ACTION_VIEW).apply {
                    //TODO: pass App Link
                    data = Uri.parse("https://web3modal-laboratory-git-chore-kotlin-assetlinks-walletconnect1.vercel.app/wallet?wc_ev=$encodedRequest")
                        .also { println("kobe: URL: $it") }
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            }
        } catch (e: Exception) {
            logger.error("Trigger request error: ${e.message}")
        }
    }

    override fun triggerResponse(topic: Topic, response: JsonRpcResponse, participants: Participants?) {
        val responseJson = serializer.serialize(response) ?: throw Exception("Null")

        println("kobe: Response: $responseJson")


        try {
            val encryptedResponse = chaChaPolyCodec.encrypt(topic, responseJson, EnvelopeType.ONE, participants)
            val encodedResponse = Base64.encodeToString(encryptedResponse, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)

            val intent = Intent(Intent.ACTION_VIEW).apply {
                //TODO: pass App Link
                data = Uri.parse("https://web3modal-laboratory-git-chore-kotlin-assetlinks-walletconnect1.vercel.app/dapp?wc_ev=$encodedResponse&topic=${topic.value}")
                    .also { println("kobe: URL Response: $it") }
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            println("kobe; wallet response id: ${response.id}")
            jsonRpcHistory.updateRequestWithResponse(response.id, responseJson)
            context.startActivity(intent) //todo: check if app link was opened properly
        } catch (e: Exception) {
            logger.error("Trigger request error: ${e.message}")
        }
    }

    override fun dispatchEnvelope(url: String) {
        val uri = Uri.parse(url)

        println("kobe: Dispatch URI: $uri")
        val encodedEnvelope = uri.getQueryParameter("wc_ev") ?: throw Exception("null")
        val topic = uri.getQueryParameter("topic")

        println("kobe: Topic: $topic")

        //todo: try/catch
        val envelope = if (topic != null) {
            chaChaPolyCodec.decrypt(Topic(topic), Base64.decode(encodedEnvelope, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING))
        } else {
            String(Base64.decode(encodedEnvelope, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING), Charsets.UTF_8)
        }

        println("kobe: Envelope: $envelope")

        scope.launch {
            supervisorScope {
                serializer.tryDeserialize<ClientJsonRpc>(envelope)?.let { clientJsonRpc ->
                    if (jsonRpcHistory.setRequest(clientJsonRpc.id, Topic(topic ?: ""), clientJsonRpc.method, envelope)) {
                        println("kobe: Request")
                        serializer.deserialize(clientJsonRpc.method, envelope)?.let {
                            println("kobe: Payload sync: $it")
                            _clientSyncJsonRpc.emit(WCRequest(Topic(topic ?: ""), clientJsonRpc.id, clientJsonRpc.method, it))
                        }
                    }
                } ?: serializer.tryDeserialize<JsonRpcResponse.JsonRpcResult>(envelope)?.let { result ->
                    println("kobe: Result")
                    val serializedResult = serializer.serialize(result) ?: throw Exception("")//?: return handleError("JsonRpcInteractor: Unknown result params")
                    val jsonRpcRecord = jsonRpcHistory.updateRequestWithResponse(result.id, serializedResult)

                    println("kobe: dapp result id: ${result.id}; $jsonRpcRecord")

                    if (jsonRpcRecord != null) {
                        println("kobe: check: ${jsonRpcRecord.method}; ${jsonRpcRecord.body}")
                        serializer.deserialize(jsonRpcRecord.method, jsonRpcRecord.body)?.let { params ->
                            println("kobe: params: $params")
                            val responseVO = JsonRpcResponse.JsonRpcResult(result.id, result = result.result)
                            _peerResponse.emit(jsonRpcRecord.toWCResponse(responseVO, params))
                        } ?: println("kobe: LOL ERROR")//handleError("JsonRpcInteractor: Unknown result params")
                    }
                } ?: serializer.tryDeserialize<JsonRpcResponse.JsonRpcError>(envelope)?.let { error ->
                    val serializedResult = serializer.serialize(error) ?: throw Exception("")//?: return handleError("JsonRpcInteractor: Unknown result params")
                    val jsonRpcRecord = jsonRpcHistory.updateRequestWithResponse(error.id, serializedResult)

                    if (jsonRpcRecord != null) {
                        serializer.deserialize(jsonRpcRecord.method, jsonRpcRecord.body)?.let { params ->
                            _peerResponse.emit(jsonRpcRecord.toWCResponse(error, params))
                        } //?: handleError("JsonRpcInteractor: Unknown error params")
                    }
                } //?: handleError("JsonRpcInteractor: Received unknown object type")
            }
        }
    }
}

interface EnvelopeDispatcherInterface {
    fun triggerRequest(payload: JsonRpcClientSync<*>)
    fun triggerResponse(topic: Topic, response: JsonRpcResponse, participants: Participants?)
    fun dispatchEnvelope(url: String)

    val clientSyncJsonRpc: SharedFlow<WCRequest>
    val peerResponse: SharedFlow<WCResponse>
    val internalErrors: SharedFlow<SDKError>
}