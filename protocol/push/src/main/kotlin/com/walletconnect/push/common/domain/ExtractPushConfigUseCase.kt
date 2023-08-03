package com.walletconnect.push.common.domain

import android.net.Uri
import com.walletconnect.android.internal.common.json_rpc.data.JsonRpcSerializer
import com.walletconnect.push.common.model.EngineDO
import com.walletconnect.push.data.wellknown.config.PushConfigDTO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

class ExtractPushConfigUseCase(private val serializer: JsonRpcSerializer,) {

    suspend operator fun invoke(dappUri: Uri): Result<List<EngineDO.PushScope.Remote>> = withContext(Dispatchers.IO) {
        val pushConfigDappUri = dappUri.run {
            if (this.path?.contains(WC_PUSH_CONFIG_JSON) == false) {
                this.buildUpon().appendPath(WC_PUSH_CONFIG_JSON)
            } else {
                this
            }
        }

        val wellKnownPushConfigString = URL(pushConfigDappUri.toString()).openStream().bufferedReader().use { it.readText() }
        val pushConfig = serializer.tryDeserialize<PushConfigDTO>(wellKnownPushConfigString) ?: return@withContext Result.failure(Exception("Failed to parse $WC_PUSH_CONFIG_JSON"))
        val pushScopeRemote = pushConfig.types.map { typeDTO ->
            EngineDO.PushScope.Remote(
                name = typeDTO.name,
                description = typeDTO.description
            )
        }

        Result.success(pushScopeRemote)
    }

    private companion object {
        const val WC_PUSH_CONFIG_JSON = ".well-known/wc-push-config.json"
    }
}