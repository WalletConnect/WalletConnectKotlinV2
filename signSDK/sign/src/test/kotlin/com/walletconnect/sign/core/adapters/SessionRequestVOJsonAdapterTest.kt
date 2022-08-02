package com.walletconnect.sign.core.adapters

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.tinder.scarlet.utils.getRawType
import com.walletconnect.sign.core.model.vo.clientsync.session.SessionRpcVO
import com.walletconnect.sign.core.model.vo.clientsync.session.payload.SessionRequestVO
import org.intellij.lang.annotations.Language
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

    @Test
    fun deserializeToNamedJsonArray() {
        @Language("JSON")
        val params = """
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
        @Language("JSON")
        val stringParamsWithNamedJsonArray = """
            {
              "id":1658389421577926,
              "jsonrpc":"2.0",
              "method":"wc_sessionRequest",
              "params":{
                "request":{
                  "method":"erd_signTransactions",
                  "params":{
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
                },
                "chainId":"elrond:D"
              }
            }
        """.trimIndent()

        val adapter = moshi.adapter(SessionRpcVO.SessionRequest::class.java)
        val deserializedJson = adapter.fromJson(stringParamsWithNamedJsonArray)
        val serializedParams = deserializedJson?.params?.request?.params

        val expectedParamsJsonObj = JSONObject(params)
        val actualParamsJsonObj = JSONObject("{$serializedParams}")
println(actualParamsJsonObj.getJSONArray("transactions").toString())
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
        @Language("JSON")
        val params = """
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
        @Language("JSON")
        val stringParamsWithNamedJsonObject = """
            {
              "id":1658389421577926,
              "jsonrpc":"2.0",
              "method":"wc_sessionRequest",
              "params":{
                "request":{
                  "method":"erd_signTransactions",
                  "params": {
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
                },
                "chainId":"elrond:D"
              }
            }
        """.trimIndent()

        val adapter = moshi.adapter(SessionRpcVO.SessionRequest::class.java)
        val deserializedJson = adapter.fromJson(stringParamsWithNamedJsonObject)
        val serializedParams = deserializedJson?.params?.request?.params

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
}