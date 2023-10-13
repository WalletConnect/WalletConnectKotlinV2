@file:JvmSynthetic

package com.walletconnect.notify.engine.domain

import android.net.Uri
import com.walletconnect.android.internal.common.explorer.domain.usecase.GetNotifyConfigUseCase
import com.walletconnect.android.internal.common.model.AppMetaData
import com.walletconnect.notify.common.model.NotificationScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class ExtractMetadataFromConfigUseCase(private val getNotifyConfigUseCase: GetNotifyConfigUseCase) {
    suspend operator fun invoke(appUri: Uri): Result<Pair<AppMetaData, List<NotificationScope.Remote>>> = withContext(Dispatchers.IO) {
        val appDomain = appUri.host ?: throw IllegalStateException("Unable to parse domain from $appUri")

        return@withContext getNotifyConfigUseCase(appDomain).mapCatching { notifyConfig ->
            Pair(
                AppMetaData(description = notifyConfig.description, url = appUri.toString(), icons = notifyConfig.icons, name = notifyConfig.name),
                notifyConfig.types.map { typeDTO -> NotificationScope.Remote(name = typeDTO.name, description = typeDTO.description, id = typeDTO.id) }
            )
        }
    }
}