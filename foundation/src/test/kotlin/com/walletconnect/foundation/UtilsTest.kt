package com.walletconnect.foundation

import com.walletconnect.util.generateClientToClientId
import com.walletconnect.util.generateClientToServerId
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

class UtilsTest {

    @Test
    fun `generate client to client id test`() {
        val id = generateClientToClientId()
        println(id)
        assertTrue { id > 0 }
        assertTrue { id.toString().length == 16 }
    }

    @Test
    fun `generate client to server id test`() {
        val id = generateClientToServerId()
        println(id)
        assertTrue { id > 0 }
        assertTrue { id.toString().length == 19 }
    }
}