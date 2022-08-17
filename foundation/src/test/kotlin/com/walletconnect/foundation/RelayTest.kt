package com.walletconnect.foundation

import com.walletconnect.foundation.crypto.data.repository.JwtRepository
import com.walletconnect.foundation.di.commonModule
import com.walletconnect.foundation.di.cryptoModule
import com.walletconnect.foundation.di.networkModule
import com.walletconnect.foundation.network.RelayInterface
import com.walletconnect.util.addUserAgent
import org.junit.jupiter.api.Test
import org.koin.core.KoinApplication
import kotlin.test.Ignore

class RelayTest {

    @Ignore
    @Test
    fun integrationTest() {
        // TODO: Create two separate koin instances and generate a new JWT for each instance when creating client
        val koinApp: KoinApplication = KoinApplication.init().apply {
            modules(
                commonModule(),
                cryptoModule()
            )
        }

        val serverUrl = "wss://relay.walletconnect.com?projectId=${"test project id"}"
        val jwt = koinApp.koin.get<JwtRepository>().generateJWT(serverUrl)
        koinApp.modules(networkModule(serverUrl.addUserAgent(), "2.0.0-rc.1", jwt))

        val clientA = koinApp.koin.get<RelayInterface>()
        val clientB = koinApp.koin.get<RelayInterface>()
    }
}