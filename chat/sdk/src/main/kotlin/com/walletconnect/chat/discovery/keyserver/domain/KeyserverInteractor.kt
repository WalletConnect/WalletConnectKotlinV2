@file:JvmSynthetic

package com.walletconnect.chat.discovery.keyserver.domain

import com.walletconnect.chat.discovery.keyserver.domain.use_case.RegisterIdentityUseCase
import com.walletconnect.chat.discovery.keyserver.domain.use_case.RegisterInviteUseCase
import com.walletconnect.chat.discovery.keyserver.domain.use_case.ResolveIdentityUseCase
import com.walletconnect.chat.discovery.keyserver.domain.use_case.ResolveInviteUseCase
import java.net.URI

internal class KeyserverInteractor(
    val url: String,
    val registerIdentity: RegisterIdentityUseCase,
    val resolveIdentity: ResolveIdentityUseCase,
    val registerInvite: RegisterInviteUseCase,
    val resolveInvite: ResolveInviteUseCase,
) {
    val domain: String?
        get() = domainFromUrl(url)

    private fun domainFromUrl(url: String): String? {
        val uri = runCatching { URI(url) }.getOrNull() ?: return null
        val domain: String = uri.host
        return if (domain.startsWith("www.")) domain.substring(4) else domain
    }
}