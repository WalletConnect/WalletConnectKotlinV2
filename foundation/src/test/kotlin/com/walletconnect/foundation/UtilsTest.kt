package com.walletconnect.foundation

import com.walletconnect.util.generateId
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

class UtilsTest {

    @Test
    fun `generate id test`() {
        val id = generateId()
        println(id)
        assertTrue { id > 0 }
        assertTrue { id.toString().length == 16 }
    }
}