package com.walletconnect.sign

import org.json.JSONArray
import org.json.JSONObject
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class SessionRequestParserTest {

    @Test
    fun testJsonArrayParsing() {
        val json =
            "[{\"from\":\"0x022c0c42a80bd19EA4cF0F94c4F9F96645759716\",\"to\":\"0x022c0c42a80bd19EA4cF0F94c4F9F96645759716\",\"data\":\"0x\",\"nonce\":0,\"gasPrice\":27000000000,\"gasLimit\":21000,\"value\":0}]"

        val params = JSONArray(json)
        val from = params.getJSONObject(0).getString("from")
        val to = params.getJSONObject(0).getString("to")
        val data = params.getJSONObject(0).getString("data")
        val none = params.getJSONObject(0).getDouble("nonce")
        val gasPrice = params.getJSONObject(0).getLong("gasPrice")
        val gasLimit = params.getJSONObject(0).getLong("gasLimit")
        val value = params.getJSONObject(0).getInt("value")

        Assertions.assertEquals("0x022c0c42a80bd19EA4cF0F94c4F9F96645759716", from)
        Assertions.assertEquals("0x022c0c42a80bd19EA4cF0F94c4F9F96645759716", to)
        Assertions.assertEquals("0x", data)
        Assertions.assertEquals(0.0, none)
        Assertions.assertEquals(27000000000, gasPrice)
        Assertions.assertEquals(21000, gasLimit)
        Assertions.assertEquals(0, value)
    }

    @Test
    fun testJsonObjectParsing() {
        val json =
            "{\"from\":\"0x022c0c42a80bd19EA4cF0F94c4F9F96645759716\",\"to\":\"0x022c0c42a80bd19EA4cF0F94c4F9F96645759716\",\"data\":\"0x\",\"nonce\":0,\"gasPrice\":27000000000,\"gasLimit\":21000,\"value\":0}"

        val params = JSONObject(json)
        val from = params.getString("from")
        val to = params.getString("to")
        val data = params.getString("data")
        val none = params.getDouble("nonce")
        val gasPrice = params.getLong("gasPrice")
        val gasLimit = params.getLong("gasLimit")
        val value = params.getInt("value")

        Assertions.assertEquals("0x022c0c42a80bd19EA4cF0F94c4F9F96645759716", from)
        Assertions.assertEquals("0x022c0c42a80bd19EA4cF0F94c4F9F96645759716", to)
        Assertions.assertEquals("0x", data)
        Assertions.assertEquals(0.0, none)
        Assertions.assertEquals(27000000000, gasPrice)
        Assertions.assertEquals(21000, gasLimit)
        Assertions.assertEquals(0, value)
    }
}