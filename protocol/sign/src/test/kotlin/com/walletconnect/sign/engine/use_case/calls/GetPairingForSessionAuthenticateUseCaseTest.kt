package com.walletconnect.sign.engine.use_case.calls

import com.walletconnect.android.Core
import com.walletconnect.android.internal.utils.currentTimeInSeconds
import com.walletconnect.android.internal.utils.fiveMinutesInSeconds
import com.walletconnect.android.pairing.client.PairingInterface
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test

class GetPairingForSessionAuthenticateUseCaseTest {
    private val pairingProtocol: PairingInterface = mockk()
    private lateinit var useCase: GetPairingForSessionAuthenticateUseCase

    @Before
    fun setUp() {
        useCase = GetPairingForSessionAuthenticateUseCase(pairingProtocol)
    }

    @Test
    fun `pairing doesn't exist`() {
        every { pairingProtocol.getPairings() } returns emptyList()
        assertThrows(Exception::class.java) {
            useCase("pairingTopic")
        }
    }

    @Test
    fun `pairing exists`() {
        every { pairingProtocol.getPairings() } returns listOf(
            Core.Model.Pairing(
                topic = "pairingTopic",
                peerAppMetaData = Core.Model.AppMetaData("name", "description", "url", listOf("icons"), "redirect", "verifyUrl"),
                relayData = null,
                relayProtocol = "relayProtocol",
                uri = "uri",
                isActive = true,
                registeredMethods = "wc_sessionAuthenticate",
                expiry = currentTimeInSeconds + fiveMinutesInSeconds
            )
        )

        val pairing = useCase("pairingTopic")
        assert(pairing.topic == "pairingTopic")
    }

    @Test
    fun `new pairing`() {
        every { pairingProtocol.create(any(), methods = "wc_sessionAuthenticate") } returns
                Core.Model.Pairing(
                    topic = "pairingTopic",
                    peerAppMetaData = Core.Model.AppMetaData("name", "description", "url", listOf("icons"), "redirect", "verifyUrl"),
                    relayData = null,
                    relayProtocol = "relayProtocol",
                    uri = "uri",
                    isActive = true,
                    registeredMethods = "wc_sessionAuthenticate",
                    expiry = currentTimeInSeconds + fiveMinutesInSeconds
                )


        val pairing = useCase(null)
        assert(pairing.topic == "pairingTopic")
    }

    @Test
    fun `pairing without authenticate`() {
        every { pairingProtocol.getPairings() } returns listOf(
            Core.Model.Pairing(
                topic = "pairingTopic",
                peerAppMetaData = Core.Model.AppMetaData("name", "description", "url", listOf("icons"), "redirect", "verifyUrl"),
                relayData = null,
                relayProtocol = "relayProtocol",
                uri = "uri",
                isActive = true,
                registeredMethods = "",
                expiry = currentTimeInSeconds + fiveMinutesInSeconds
            )
        )

        assertThrows(Exception::class.java) {
            useCase("pairingTopic")
        }
    }
}