package com.walletconnect.sign.adapters

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.tinder.scarlet.utils.getRawType
import com.walletconnect.sign.common.adapters.SessionEventVOJsonAdapter
import com.walletconnect.sign.common.model.vo.clientsync.session.SignRpc
import com.walletconnect.sign.common.model.vo.clientsync.session.payload.SessionEventVO
import com.walletconnect.sign.util.iterateJsonArrays
import junit.framework.TestCase
import org.json.JSONArray
import org.junit.Test
import kotlin.reflect.jvm.jvmName

class SessionEventVOJsonAdapterTest {
    private val moshi: Moshi = Moshi.Builder()
        .add { type, _, moshi ->
            when (type.getRawType().name) {
                SessionEventVO::class.jvmName -> SessionEventVOJsonAdapter(moshi)
                else -> null
            }
        }
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private lateinit var data: Any
    private val stringParamsWithNamedJsonArray by lazy {
        """
          {
            "id":1659532494915,
            "jsonrpc":"2.0",
            "method":"wc_sessionEvent",
            "params": {
              "event": {
                "name":"accountsChanged",
                "data":$data
              },
              "chainId":"eip155:1"
            }
          }
        """.trimIndent()
    }
    private val adapter by lazy { moshi.adapter(SignRpc.SessionEvent::class.java) }
    private val deserializedJson by lazy { adapter.fromJson(stringParamsWithNamedJsonArray) }
    private val serializedData by lazy { requireNotNull(deserializedJson?.params?.event?.data) }

    @Test
    fun testParsingJsonArrayAccounts() {
        data = """
            [
              "0x4d7920656d61696c206973206a6f686e40646f652e636f6d202d2031363539353332343934303431", 
              "0x022c0c42a80bd19EA4cF0F94c4F9F96645759716"
            ]
        """.trimIndent()

        val expectedParamsJsonArray = JSONArray(data.toString())
        val actualParamsJsonArray = JSONArray(serializedData.toString())

        TestCase.assertEquals(expectedParamsJsonArray.length(), actualParamsJsonArray.length())

        iterateJsonArrays(expectedParamsJsonArray, actualParamsJsonArray)
    }
}