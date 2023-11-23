package com.walletconnect.web3.wallet.use_case

import com.walletconnect.android.echo.DecryptMessageUseCaseInterface
import com.walletconnect.android.echo.Message
import com.walletconnect.android.internal.common.crypto.codec.Codec
import com.walletconnect.android.internal.common.json_rpc.data.JsonRpcSerializer
import com.walletconnect.android.internal.common.storage.MetadataStorageRepositoryInterface

internal class DecryptWeb3WalletMessageUseCase(
    private val codec: Codec,
    private val serializer: JsonRpcSerializer,
    private val metadataRepository: MetadataStorageRepositoryInterface
) : DecryptMessageUseCaseInterface {
    override suspend fun decryptMessage(topic: String, message: String, onSuccess: (Message) -> Unit, onFailure: (Throwable) -> Unit) {

    }
}