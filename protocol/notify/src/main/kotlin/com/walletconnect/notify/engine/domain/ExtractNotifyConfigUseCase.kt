@file:JvmSynthetic

package com.walletconnect.notify.engine.domain

import android.net.Uri
import com.walletconnect.android.internal.common.json_rpc.data.JsonRpcSerializer
import com.walletconnect.notify.common.model.NotificationScope
import com.walletconnect.notify.data.wellknown.config.NotifyConfigDTO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

internal class ExtractNotifyConfigUseCase(private val serializer: JsonRpcSerializer,) {

    suspend operator fun invoke(dappUri: Uri): Result<List<NotificationScope.Remote>> = withContext(Dispatchers.IO) {
        val notifyConfigDappUri = dappUri.run {
            if (this.path?.contains(WC_NOTIFY_CONFIG_JSON) == false) {
                this.buildUpon().appendPath(WC_NOTIFY_CONFIG_JSON)
            } else {
                this
            }
        }

        val wellKnownNotifyConfigString = URL(notifyConfigDappUri.toString()).openStream().bufferedReader().use { it.readText() }
        val notifyConfig = serializer.tryDeserialize<NotifyConfigDTO>(wellKnownNotifyConfigString) ?: return@withContext Result.failure(Exception("Failed to parse $WC_NOTIFY_CONFIG_JSON"))
        val notificationScopeRemote = notifyConfig.types.map { typeDTO ->
            NotificationScope.Remote(
                name = typeDTO.name,
                description = typeDTO.description
            )
        }

        Result.success(notificationScopeRemote)
    }

    private companion object {
        const val WC_NOTIFY_CONFIG_JSON = ".well-known/wc-notify-config.json"
    }
}