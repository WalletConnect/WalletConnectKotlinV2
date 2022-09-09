package com.walletconnect.sign.adapters

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

private val moshi: Moshi = Moshi.Builder()
    .addLast(KotlinJsonAdapterFactory())
    .build()

class SessionRequestParamsParsingTest {

    private val sessionRequestJsonParams1 =
        "[{\"from\":\"0x022c0c42a80bd19EA4cF0F94c4F9F96645759716\",\"to\":\"0x022c0c42a80bd19EA4cF0F94c4F9F96645759716\",\"data\":\"0x\",\"nonce\":0,\"gasPrice\":27000000000,\"gasLimit\":21000,\"value\":0}]"

    @Test
    fun `parse send transaction test`() {
        val sendTx = moshi.adapter(List::class.java).fromJson(sessionRequestJsonParams1)

        if (sendTx != null) {
            val from = (sendTx[0] as Map<String, *>).getValue("from")
            val to = (sendTx[0] as Map<String, *>).getValue("to")
            val nonce = (sendTx[0] as Map<String, *>).getValue("nonce")
            val data = (sendTx[0] as Map<String, *>).getValue("data")
            val gasPrice = (sendTx[0] as Map<String, Long>).getValue("gasPrice")
            val gasLimit = (sendTx[0] as Map<String, Long>).getValue("gasLimit")
            val value = (sendTx[0] as Map<String, Int>).getValue("value")

            Assertions.assertEquals("0x022c0c42a80bd19EA4cF0F94c4F9F96645759716", from)
            Assertions.assertEquals("0x022c0c42a80bd19EA4cF0F94c4F9F96645759716", to)
            Assertions.assertEquals("0x", data)
            Assertions.assertEquals(0.0, nonce)
            Assertions.assertEquals(27000000000, gasPrice)
            Assertions.assertEquals(21000, gasLimit)
            Assertions.assertEquals(0, value)
        }
    }
}