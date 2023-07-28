package com.walletconnect.push.engine.calls

import com.walletconnect.android.internal.common.crypto.codec.Codec
import com.walletconnect.android.internal.common.json_rpc.data.JsonRpcSerializer
import com.walletconnect.android.internal.common.model.params.PushParams
import com.walletconnect.android.internal.common.model.sync.ClientJsonRpc
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.push.common.model.EngineDO
import com.walletconnect.push.common.model.toEngineDO
import kotlinx.coroutines.supervisorScope
import kotlin.reflect.full.safeCast

internal class DecryptMessageUseCase(
    private val codec: Codec,
    private val serializer: JsonRpcSerializer,
): DecryptMessageUseCaseInterface {

    override suspend fun decryptMessage(topic: String, message: String, onSuccess: (EngineDO.PushMessage) -> Unit, onFailure: (Throwable) -> Unit) = supervisorScope {
        try {
            val decryptedMessageString = codec.decrypt(Topic(topic), message)
            // TODO: How to look in JsonRpcHistory for dupes without Rpc ID
            val clientJsonRpc = serializer.tryDeserialize<ClientJsonRpc>(decryptedMessageString) ?: return@supervisorScope onFailure(IllegalArgumentException("The decrypted message does not match WalletConnect JSON-RPC format"))
            val pushMessage = serializer.deserialize(clientJsonRpc.method, decryptedMessageString)
            val pushMessageEngineDO = PushParams.MessageParams::class.safeCast(pushMessage)?.toEngineDO() ?: return@supervisorScope onFailure(IllegalArgumentException("The decrypted message does not match WalletConnect Push Message format"))

            onSuccess(pushMessageEngineDO)
        } catch (e: Exception) {
            onFailure(e)
        }
    }
}

internal interface DecryptMessageUseCaseInterface {
    suspend fun decryptMessage(topic: String, message: String, onSuccess: (EngineDO.PushMessage) -> Unit, onFailure: (Throwable) -> Unit)
}