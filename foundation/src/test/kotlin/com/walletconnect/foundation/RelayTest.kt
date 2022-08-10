package com.walletconnect.foundation

import com.walletconnect.foundation.crypto.data.repository.JwtRepository
import com.walletconnect.foundation.di.commonModule
import com.walletconnect.foundation.di.cryptoModule
import com.walletconnect.foundation.di.scarletModule
import com.walletconnect.foundation.network.RelayInterface
import com.walletconnect.util.addUserAgent
import org.junit.jupiter.api.Test
import org.koin.core.KoinApplication

class RelayTest {

    @Test
    fun integrationTest() {
        val koinApp: KoinApplication = KoinApplication.init().apply {
            modules(
                commonModule(),
                cryptoModule()
            )
        }

        val serverUrl = "wss://relay.walletconnect.com?projectId=${"test project id"}"
        val jwt = koinApp.koin.get<JwtRepository>().generateJWT(serverUrl)
        koinApp.modules(scarletModule(serverUrl.addUserAgent(), jwt))

        val clientA = koinApp.koin.get<RelayInterface>()
        val clientB = koinApp.koin.get<RelayInterface>()
    }
}