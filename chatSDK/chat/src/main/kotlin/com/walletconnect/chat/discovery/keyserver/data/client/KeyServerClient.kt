@file:JvmSynthetic

package com.walletconnect.chat.discovery.keyserver.data.client

import com.walletconnect.chat.discovery.keyserver.data.service.KeyServerService
import com.walletconnect.chat.discovery.keyserver.model.KeyServerDTO

internal class KeyServerClient(
    private val service: KeyServerService,
) {
    suspend fun register(account: KeyServerDTO.Account) = service.register(account)
    suspend fun resolve(account: String): KeyServerDTO.Account = service.resolve(account)
}