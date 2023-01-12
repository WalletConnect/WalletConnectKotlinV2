@file:JvmSynthetic

package com.walletconnect.chat.discovery.keyserver.domain

import com.walletconnect.android.internal.common.cacao.Cacao
import com.walletconnect.chat.common.model.AccountId
import com.walletconnect.chat.discovery.keyserver.domain.use_case.RegisterIdentityUseCase
import com.walletconnect.chat.discovery.keyserver.domain.use_case.RegisterInviteUseCase
import com.walletconnect.chat.discovery.keyserver.domain.use_case.ResolveIdentityUseCase
import com.walletconnect.chat.discovery.keyserver.domain.use_case.ResolveInviteUseCase
import java.net.URI

internal class KeyserverInteractor(
    val url: String,
    private val registerIdentityUseCase: RegisterIdentityUseCase,
    private val resolveIdentityUseCase: ResolveIdentityUseCase,
    private val registerInviteUseCase: RegisterInviteUseCase,
    private val resolveInviteUseCase: ResolveInviteUseCase,
) {
    val domain: String?
        get() = domainFromUrl(url)

    suspend fun registerInvite(idAuth: String) = registerInviteUseCase(idAuth)
    suspend fun resolveInvite(accountId: AccountId) = resolveInviteUseCase(accountId)

    suspend fun registerIdentity(cacao: Cacao) = registerIdentityUseCase(cacao)
    suspend fun resolveIdentity(identityKey: String) = resolveIdentityUseCase(identityKey)

    private fun domainFromUrl(url: String): String? {
        val uri = runCatching { URI(url) }.getOrNull() ?: return null
        val domain: String = uri.host
        return if (domain.startsWith("www.")) domain.substring(4) else domain
    }
}