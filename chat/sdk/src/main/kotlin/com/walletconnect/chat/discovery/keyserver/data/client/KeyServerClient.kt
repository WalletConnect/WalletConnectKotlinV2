@file:JvmSynthetic

package com.walletconnect.chat.discovery.keyserver.data.client

import com.walletconnect.android.internal.common.cacao.Cacao
import com.walletconnect.chat.discovery.keyserver.data.service.KeyServerService

internal class KeyServerClient(
    private val service: KeyServerService,
) {
    suspend fun registerInvite(idAuth: String) = service.registerInvite(KeyServerService.RegisterInviteBody(idAuth))
    suspend fun resolveInvite(account: String) = service.resolveInvite(account)
    suspend fun registerIdentity(cacao: Cacao) = service.registerIdentity(KeyServerService.RegisterIdentityBody(cacao))
    suspend fun resolveIdentity(identityKey: String) = service.resolveIdentity(identityKey)
}
