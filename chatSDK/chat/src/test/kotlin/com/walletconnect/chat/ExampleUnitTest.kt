package com.walletconnect.chat

import com.walletconnect.chat.client.ChatClient
import com.walletconnect.chat.client.ChatProtocol
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun `Creation of multiple ChatClients`() {
        val firstPeerClient = ChatClient
        val secondPeerClient = ChatProtocol()

        assertNotEquals(secondPeerClient, firstPeerClient)
    }

    @Test
    fun `ChatClient is singleton`() {
        val firstPeerClient = ChatClient
        val secondPeerClient = ChatClient

        assertEquals(secondPeerClient, firstPeerClient)
    }
}