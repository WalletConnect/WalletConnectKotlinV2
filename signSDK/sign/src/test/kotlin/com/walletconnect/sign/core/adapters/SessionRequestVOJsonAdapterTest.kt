package com.walletconnect.sign.core.adapters

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.tinder.scarlet.utils.getRawType
import com.walletconnect.android_core.common.adapters.SessionRequestVOJsonAdapter
import com.walletconnect.sign.core.model.vo.clientsync.session.SessionRpcVO
import com.walletconnect.sign.core.model.vo.clientsync.session.payload.SessionRequestVO
import org.intellij.lang.annotations.Language
import org.json.JSONArray
import org.json.JSONObject
import org.junit.jupiter.api.Test
import kotlin.reflect.jvm.jvmName
import kotlin.test.assertEquals

internal class SessionRequestVOJsonAdapterTest {
    private val moshi: Moshi = Moshi.Builder()
        .add { type, _, moshi ->
            when (type.getRawType().name) {
                SessionRequestVO::class.jvmName -> SessionRequestVOJsonAdapter(moshi)
                else -> null
            }
        }
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private lateinit var params: String
    private val stringParamsWithNamedJsonArray by lazy {
        """
          {
            "id":1659532494915,
            "jsonrpc":"2.0",
            "method":"wc_sessionRequest",
            "params": {
              "request": {
                "method":"personal_sign",
                "params": $params
              },
              "chainId":"eip155:1"
            }
          }
        """.trimIndent()
    }
    private val adapter by lazy { moshi.adapter(SessionRpcVO.SessionRequest::class.java) }
    private val deserializedJson by lazy { adapter.fromJson(stringParamsWithNamedJsonArray) }
    private val serializedParams by lazy { deserializedJson?.params?.request?.params }

    @Test
    fun deserializeToNamedJsonArray() {
        params = """
            {
                "transactions":[
                      {
                        "nonce":21,
                        "value":"10000000000000000",
                        "receiver":"erd1c542gysqkmfqwwwu7tkuhz8kp9yxkg32gzsuvgmryrs74nhhltzqkna8na",
                        "sender":"erd1c542gysqkmfqwwwu7tkuhz8kp9yxkg32gzsuvgmryrs74nhhltzqkna8na",
                        "gasPrice":1000000000,
                        "gasLimit":60000000,
                        "data":"Zmlyc3Q=",
                        "chainID":"D",
                        "version":1
                      },
                      {
                        "nonce":"0x0",
                        "value":"0x0",
                        "receiver":"erd1c542gysqkmfqwwwu7tkuhz8kp9yxkg32gzsuvgmryrs74nhhltzqkna8na",
                        "sender":"erd1c542gysqkmfqwwwu7tkuhz8kp9yxkg32gzsuvgmryrs74nhhltzqkna8na",
                        "gasPrice":"0x4a817c800",
                        "gasLimit":"0x5208",
                        "data":"c2Vjb25k",
                        "chainID":"D",
                        "version":1
                      },
                      {
                        "nonce":23,
                        "value":"30000000000000000",
                        "receiver":"erd1c542gysqkmfqwwwu7tkuhz8kp9yxkg32gzsuvgmryrs74nhhltzqkna8na",
                        "sender":"erd1c542gysqkmfqwwwu7tkuhz8kp9yxkg32gzsuvgmryrs74nhhltzqkna8na",
                        "gasPrice":1000000000,
                        "gasLimit":60000000,
                        "data":"dGhpcmQ=",
                        "chainID":"D",
                        "version":1.1,
                        "testBoolean":true
                      }
                ]
            }
        """.trimIndent()

        val expectedParamsJsonObj = JSONObject(params)
        val actualParamsJsonObj = JSONObject("{$serializedParams}")

        assertEquals(expectedParamsJsonObj.getJSONArray("transactions").length(), actualParamsJsonObj.getJSONArray("transactions").length())

        with(expectedParamsJsonObj.getJSONArray("transactions")) exp@{
            with(actualParamsJsonObj.getJSONArray("transactions")) act@{
                (0 until this@exp.length()).forEach { index ->
                    val expObj = this@exp.getJSONObject(index)

                    expObj.keys().forEach { key ->
                        assertEquals(expObj.get(key), this@act.getJSONObject(index).get(key))
                    }
                }
            }
        }
    }

    @Test
    fun deserializeToNamedJsonObject() {
        params = """
            {
                "nonce":21,
                "value":"10000000000000000",
                "receiver":"erd1c542gysqkmfqwwwu7tkuhz8kp9yxkg32gzsuvgmryrs74nhhltzqkna8na",
                "sender":"erd1c542gysqkmfqwwwu7tkuhz8kp9yxkg32gzsuvgmryrs74nhhltzqkna8na",
                "gasPrice":1000000000,
                "gasLimit":60000000,
                "data":"Zmlyc3Q=",
                "chainID":"D",
                "version":1
            }
        """.trimIndent()

        val expectedParamsJsonObj = JSONObject(params)
        val actualParamsJsonObj = JSONObject(serializedParams)

        assertEquals(expectedParamsJsonObj.length(), actualParamsJsonObj.length())

        with(expectedParamsJsonObj) exp@{
            with(actualParamsJsonObj) act@{
                this@exp.keys().forEach { key ->
                    assertEquals(this@exp.get(key), this@act.get(key))
                }
            }
        }
    }

    @Test
    fun deserializeToUnNamedJsonArray() {
        params = """
            [
              "0x4d7920656d61696c206973206a6f686e40646f652e636f6d202d2031363539353332343934303431", 
              "0x022c0c42a80bd19EA4cF0F94c4F9F96645759716"
            ]
        """.trimIndent()

        val expectedParamsJsonArry = JSONArray(params)
        val actualParamsJsonArry = JSONArray("$serializedParams")

        assertEquals(expectedParamsJsonArry.length(), actualParamsJsonArry.length())

        with(expectedParamsJsonArry) exp@{
            with(actualParamsJsonArry) act@{
                (0 until this@exp.length()).forEach { index ->
                    assertEquals(this@exp.getString(index), this@act.getString(index))
                }
            }
        }
    }

    @Test
    fun deserializeToUnNamedJsonArrayMixedType() {
        params = """
            [
              "0xCD2a3d9F938E13CD947Ec05AbC7FE734Df8DD826",
              {
                "types": {
                  "EIP712Domain": [
                    {
                      "name": "name",
                      "type": "string"
                    },
                    {
                      "name": "version",
                      "type": "string"
                    },
                    {
                      "name": "chainId",
                      "type": "uint256"
                    },
                    {
                      "name": "verifyingContract",
                      "type": "address"
                    }
                  ],
                  "Person": [
                    {
                      "name": "name",
                      "type": "string"
                    },
                    {
                      "name": "wallet",
                      "type": "address"
                    }
                  ],
                  "Mail": [
                    {
                      "name": "from",
                      "type": "Person"
                    },
                    {
                      "name": "to",
                      "type": "Person"
                    },
                    {
                      "name": "contents",
                      "type": "string"
                    }
                  ]
                },
                "primaryType": "Mail",
                "domain": {
                  "name": "Ether Mail",
                  "version": "1",
                  "chainId": 1,
                  "verifyingContract": "0xCcCCccccCCCCcCCCCCCcCcCccCcCCCcCcccccccC"
                },
                "message": {
                  "from": {
                    "name": "Cow",
                    "wallet": "0xCD2a3d9F938E13CD947Ec05AbC7FE734Df8DD826"
                  },
                  "to": {
                    "name": "Bob",
                    "wallet": "0xbBbBBBBbbBBBbbbBbbBbbbbBBbBbbbbBbBbbBBbB"
                  },
                  "contents": "Hello, Bob!"
                }
              }
            ]
        """.trimIndent()

        val expectedParamsJsonArry = JSONArray(params)
        val actualParamsJsonArry = JSONArray("$serializedParams")

        assertEquals(expectedParamsJsonArry.length(), actualParamsJsonArry.length())

        with(expectedParamsJsonArry) exp@{
            with(actualParamsJsonArry) act@{
                (0 until this@exp.length()).forEach { index ->
                    when (val currentItem = this@exp.get(index)) {
                        is JSONObject -> iterateJsonObjects(currentItem, this@act.getJSONObject(index))
                        is JSONArray -> iterateJsonArrays(currentItem, this@act.getJSONArray(index))
                        else -> assertEquals(currentItem, this@act.get(index))
                    }
                }
            }
        }
    }

    @Test
    fun ethSignedTypedData() {
        // Malformed JSON with mixed in escaping characters from JS Dapp
        params = """
            ["0x022c0c42a80bd19EA4cF0F94c4F9F96645759716","{\"types\":{\"EIP712Domain\":[{\"name\":\"name\",\"type\":\"string\"},{\"name\":\"version\",\"type\":\"string\"},{\"name\":\"chainId\",\"type\":\"uint256\"},{\"name\":\"verifyingContract\",\"type\":\"address\"}],\"Person\":[{\"name\":\"name\",\"type\":\"string\"},{\"name\":\"wallet\",\"type\":\"address\"}],\"Mail\":[{\"name\":\"from\",\"type\":\"Person\"},{\"name\":\"to\",\"type\":\"Person\"},{\"name\":\"contents\",\"type\":\"string\"}]},\"primaryType\":\"Mail\",\"domain\":{\"name\":\"Ether Mail\",\"version\":\"1\",\"chainId\":1,\"verifyingContract\":\"0xCcCCccccCCCCcCCCCCCcCcCccCcCCCcCcccccccC\"},\"message\":{\"from\":{\"name\":\"Cow\",\"wallet\":\"0xCD2a3d9F938E13CD947Ec05AbC7FE734Df8DD826\"},\"to\":{\"name\":\"Bob\",\"wallet\":\"0xbBbBBBBbbBBBbbbBbbBbbbbBBbBbbbbBbBbbBBbB\"},\"contents\":\"Hello, Bob!\"}}"]
        """.trimIndent()
        @Language("JSON")
        val expectedSerializableParams = """
            ["0x022c0c42a80bd19EA4cF0F94c4F9F96645759716",{"types":{"EIP712Domain":[{"name":"name","type":"string"},{"name":"version","type":"string"},{"name":"chainId","type":"uint256"},{"name":"verifyingContract","type":"address"}],"Person":[{"name":"name","type":"string"},{"name":"wallet","type":"address"}],"Mail":[{"name":"from","type":"Person"},{"name":"to","type":"Person"},{"name":"contents","type":"string"}]},"primaryType":"Mail","domain":{"name":"Ether Mail","version":"1","chainId":1,"verifyingContract":"0xCcCCccccCCCCcCCCCCCcCcCccCcCCCcCcccccccC"},"message":{"from":{"name":"Cow","wallet":"0xCD2a3d9F938E13CD947Ec05AbC7FE734Df8DD826"},"to":{"name":"Bob","wallet":"0xbBbBBBBbbBBBbbbBbbBbbbbBBbBbbbbBbBbbBBbB"},"contents":"Hello, Bob!"}}]
        """.trimIndent()
        val expectedParamsJsonArry = JSONArray(expectedSerializableParams)
        val actualParamsJsonArry = JSONArray("$serializedParams")

        assertEquals(expectedParamsJsonArry.length(), actualParamsJsonArry.length())

        with(expectedParamsJsonArry) exp@{
            with(actualParamsJsonArry) act@{
                (0 until this@exp.length()).forEach { index ->
                    when (val currentItem = this@exp.get(index)) {
                        is JSONObject -> iterateJsonObjects(currentItem, this@act.getJSONObject(index))
                        is JSONArray -> iterateJsonArrays(currentItem, this@act.getJSONArray(index))
                        else -> assertEquals(currentItem, this@act.get(index))
                    }
                }
            }
        }
    }

    private fun iterateJsonObjects(expJsonObject: JSONObject, actJsonObject: JSONObject) {
        expJsonObject.keys().forEach { key ->
            assert(actJsonObject.has(key))
            val expCurrentItem = expJsonObject.get(key)
            val actCurrentItem = actJsonObject.get(key)

            when {
                expCurrentItem is JSONObject && actCurrentItem is JSONObject -> {
                    iterateJsonObjects(expCurrentItem, actCurrentItem)
                }
                expCurrentItem is JSONArray && actCurrentItem is JSONArray -> {
                    iterateJsonArrays(expCurrentItem, actCurrentItem)
                }
                else -> assertEquals(expCurrentItem, actCurrentItem)
            }
        }
    }

    private fun iterateJsonArrays(expJsonArray: JSONArray, actJsonArray: JSONArray) {
        assertEquals(expJsonArray.length(), actJsonArray.length())

        (0 until expJsonArray.length()).forEach { index ->
            val expCurrentIndexItem = expJsonArray.get(index)
            val actCurrentIndexItem = actJsonArray[index]

            when {
                expCurrentIndexItem is JSONObject && actCurrentIndexItem is JSONObject -> iterateJsonObjects(expCurrentIndexItem, actCurrentIndexItem)
                expCurrentIndexItem is JSONArray && actCurrentIndexItem is JSONArray -> iterateJsonArrays(expCurrentIndexItem, actCurrentIndexItem)
                else -> assertEquals(expCurrentIndexItem, actCurrentIndexItem)
            }
        }
    }
}