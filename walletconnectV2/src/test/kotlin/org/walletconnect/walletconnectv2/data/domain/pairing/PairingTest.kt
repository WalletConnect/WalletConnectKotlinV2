package org.walletconnect.walletconnectv2.data.domain.pairing

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.walletconnect.walletconnectv2.data.domain.pairing.Pairing.Companion.toPairProposal

internal class PairingTest {

    @Test
    fun `Given a Dapp wants to start pairing, When a proper URI is provided, Then a PairingProposal is produced`() {
        val testUri = "wc:0b1a3d6c0336662dddb6278ee0aa25380569b79e7e86cfe39fb20b4b189096a0@2?controller=false&publicKey=66db1bd5fad65392d1d5a4856d0d549d2fca9194327138b41c289b961d147860&relay=%7B%22protocol%22%3A%22waku%22%7D"
        val pairingProposal = testUri.toPairProposal()

        Assertions.assertNotNull(pairingProposal)
        assert(pairingProposal.topic.isNotBlank())
        Assertions.assertFalse(pairingProposal.relay.isEmpty)
        assert(pairingProposal.pairingProposer.publicKey.isNotBlank())
        assert(pairingProposal.ttl > 0)
    }
}