@file:JvmSynthetic

package com.walletconnect.notify.engine.domain

import android.net.Uri
import android.util.Base64
import com.walletconnect.android.internal.common.json_rpc.data.JsonRpcSerializer
import com.walletconnect.android.internal.common.model.AppMetaData
import com.walletconnect.foundation.common.model.PublicKey
import com.walletconnect.foundation.util.Logger
import com.walletconnect.notify.common.model.NotificationScope
import com.walletconnect.notify.data.wellknown.config.NotifyConfigDTO
import com.walletconnect.notify.data.wellknown.did.DidJsonDTO
import com.walletconnect.notify.data.wellknown.did.VerificationMethodDTO
import com.walletconnect.notify.engine.calls.DidJsonPublicKeyPair
import com.walletconnect.util.bytesToHex
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

internal class ExtractMetadataFromConfigUseCase(
    private val serializer: JsonRpcSerializer,
    private val generateAppropriateUri: GenerateAppropriateUriUseCase,
    private val logger: Logger
) {

    suspend operator fun invoke(dappUri: Uri): Result<Pair<AppMetaData, List<NotificationScope.Remote>>> = withContext(Dispatchers.IO) {
        val notifyConfigDappUri = generateAppropriateUri(dappUri, WC_NOTIFY_CONFIG_JSON)


        return@withContext notifyConfigDappUri.runCatching {
            // Get the did.json from the dapp
            URL(this.toString()).openStream().bufferedReader().use { it.readText() }
        }.mapCatching { wellKnownNotifyConfigString ->
            // Parse the did.json
            serializer.tryDeserialize<NotifyConfigDTO>(wellKnownNotifyConfigString)
                ?: throw Exception("Failed to parse $WC_NOTIFY_CONFIG_JSON. Check that the $$WC_NOTIFY_CONFIG_JSON file matches the specs")
        }.mapCatching { notifyConfig ->
            Pair(
                AppMetaData(notifyConfig.description, dappUri.toString(), notifyConfig.icons, notifyConfig.name),
                notifyConfig.types.map { typeDTO ->
                    NotificationScope.Remote(
                        name = typeDTO.name,
                        description = typeDTO.description
                    )
                }
            )
        }
    }


    private companion object {
        const val WC_NOTIFY_CONFIG_JSON = ".well-known/wc-notify-config.json"
    }
}