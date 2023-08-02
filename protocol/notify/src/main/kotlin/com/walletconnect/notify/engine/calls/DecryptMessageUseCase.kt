@file:JvmSynthetic

package com.walletconnect.notify.engine.calls

import com.walletconnect.android.internal.common.crypto.codec.Codec
import com.walletconnect.android.internal.common.json_rpc.data.JsonRpcSerializer
import com.walletconnect.android.internal.common.model.params.NotifyParams
import com.walletconnect.android.internal.common.model.sync.ClientJsonRpc
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.notify.common.model.EngineDO
import com.walletconnect.notify.common.model.toEngineDO
import kotlinx.coroutines.supervisorScope
import kotlin.reflect.safeCast

internal class DecryptMessageUseCase(
    private val codec: Codec,
    private val serializer: JsonRpcSerializer,
): DecryptMessageUseCaseInterface {

    override suspend fun decryptMessage(topic: String, message: String, onSuccess: (EngineDO.Message) -> Unit, onFailure: (Throwable) -> Unit) = supervisorScope {
        try {
            val decryptedMessageString = codec.decrypt(Topic(topic), message)
            // TODO: How to look in JsonRpcHistory for dupes without Rpc ID
            val clientJsonRpc = serializer.tryDeserialize<ClientJsonRpc>(decryptedMessageString) ?: return@supervisorScope onFailure(IllegalArgumentException("The decrypted message does not match WalletConnect JSON-RPC format"))
            val notifyMessage = serializer.deserialize(clientJsonRpc.method, decryptedMessageString)
            val notifyMessageEngineDO = NotifyParams.MessageParams::class.safeCast(notifyMessage)?.toEngineDO() ?: return@supervisorScope onFailure(IllegalArgumentException("The decrypted message does not match WalletConnect Notify Message format"))

            onSuccess(notifyMessageEngineDO)
        } catch (e: Exception) {
            onFailure(e)
        }
    }
}

internal interface DecryptMessageUseCaseInterface {
    suspend fun decryptMessage(topic: String, message: String, onSuccess: (EngineDO.Message) -> Unit, onFailure: (Throwable) -> Unit)
}