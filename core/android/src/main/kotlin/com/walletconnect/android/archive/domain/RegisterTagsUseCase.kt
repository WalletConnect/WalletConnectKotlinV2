package com.walletconnect.android.archive.domain

import com.walletconnect.android.archive.network.ArchiveServerService
import com.walletconnect.android.archive.network.model.register.RegisterBody
import com.walletconnect.android.internal.common.jwt.clientid.GenerateJwtStoreClientIdUseCase
import com.walletconnect.android.internal.common.model.Tags
import com.walletconnect.foundation.util.Logger

internal class RegisterTagsUseCase(
    private val service: ArchiveServerService,
    private val generateJwtStoreClientIdUseCase: GenerateJwtStoreClientIdUseCase,
    private val historyServerUrl: String,
    private val logger: Logger,
) {
    suspend operator fun invoke(tags: List<Tags>, relayUrl: String): Result<Unit> = runCatching {
        val authorization = BEARER_PREFIX + generateJwtStoreClientIdUseCase(historyServerUrl)
        val tagsAsStrings = tags.map { tag -> tag.id.toString() }

        with(service.register(RegisterBody(tagsAsStrings, relayUrl), authorization)) {
            if (isSuccessful && body() != null) {
                if (body()!!.status == SUCCESS_STATUS) {
                    return Result.success(Unit)
                } else {
                    throw Throwable(body()!!.errors?.first()?.message)
                }
            } else {
                throw Throwable(errorBody()?.string())
            }
        }
    }

    private companion object {
        const val SUCCESS_STATUS = "SUCCESS"
        const val BEARER_PREFIX = "Bearer "
    }
}

