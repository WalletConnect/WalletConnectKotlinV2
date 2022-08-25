package com.walletconnect.sign.core.adapters

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.tinder.scarlet.utils.getRawType
import com.walletconnect.sign.common.adapters.SessionRequestVOJsonAdapter
import com.walletconnect.sign.common.model.vo.clientsync.session.SessionRpcVO
import com.walletconnect.sign.common.model.vo.clientsync.session.payload.SessionRequestVO
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
    private val serializedParams by lazy { requireNotNull(deserializedJson?.params?.request?.params) }

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

        iterateJsonObjects(expectedParamsJsonObj, actualParamsJsonObj)
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

        iterateJsonObjects(expectedParamsJsonObj, actualParamsJsonObj)
    }

    @Test
    fun deserializeToUnNamedJsonArray() {
        params = """
            [
              "0x4d7920656d61696c206973206a6f686e40646f652e636f6d202d2031363539353332343934303431", 
              "0x022c0c42a80bd19EA4cF0F94c4F9F96645759716"
            ]
        """.trimIndent()

        val expectedParamsJsonArray = JSONArray(params)
        val actualParamsJsonArray = JSONArray(serializedParams)

        assertEquals(expectedParamsJsonArray.length(), actualParamsJsonArray.length())

        iterateJsonArrays(expectedParamsJsonArray, actualParamsJsonArray)
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

        val expectedParamsJsonArray = JSONArray(params)
        val actualParamsJsonArray = JSONArray(serializedParams)

        assertEquals(expectedParamsJsonArray.length(), actualParamsJsonArray.length())

        iterateJsonArrays(expectedParamsJsonArray, actualParamsJsonArray)
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
        val expectedParamsJsonArray = JSONArray(expectedSerializableParams)
        val actualParamsJsonArray = JSONArray(serializedParams)

        assertEquals(expectedParamsJsonArray.length(), actualParamsJsonArray.length())

        iterateJsonArrays(expectedParamsJsonArray, actualParamsJsonArray)
    }

    @Test
    fun ethSignTransaction() {
        @Language("JSON")
        params = """[{"from":"0x022c0c42a80bd19EA4cF0F94c4F9F96645759716","to":"0x022c0c42a80bd19EA4cF0F94c4F9F96645759716","data":"0x","nonce":"0x00","gasPrice":"0x9502f907","gasLimit":"0x5208","value":"0x00"}]""".trimIndent()

        val expectedParamsJsonArray = JSONArray(params)
        val actualParamsJsonArray = JSONArray(serializedParams)

        assertEquals(expectedParamsJsonArray.length(), actualParamsJsonArray.length())

        iterateJsonArrays(expectedParamsJsonArray, actualParamsJsonArray)
    }

    @Test
    fun stringifiedJsonArrayWithNulls() {
        params = """
            ["{\"f_type\":\"Signable\",\"f_vsn\":\"1.0.1\",\"message\":\"464c4f572d56302e302d7472616e73616374696f6e0000000000000000000000f90162f9015eb8cc0a2020202020202020202020207472616e73616374696f6e28613a20537472696e672c20623a20537472696e6729207b0a20202020202020202020202020207072657061726528616363743a20417574684163636f756e7429207b0a202020202020202020202020202020206c6f672861636374290a202020202020202020202020202020206c6f672861290a202020202020202020202020202020206c6f672862290a20202020202020202020202020207d0a2020202020202020202020207d0a20202020202020202020f84ca17b2274797065223a22537472696e67222c2276616c7565223a2248656c6c6f227da97b2274797065223a22537472696e67222c2276616c7565223a2257616c6c6574436f6e6e656374227da07fafbea3a826b777247a14d4b8b950f6d7a84f2be8eba0214a282ceda5240ab28203e788c60b85621495cfac801588c60b85621495cfacc988c60b85621495cfacc0\",\"addr\":\"0xc60b85621495cfac\",\"keyId\":0,\"roles\":{\"proposer\":true,\"authorizer\":true,\"payer\":true,\"param\":false},\"cadence\":\"\\n            transaction(a: String, b: String) {\\n              prepare(acct: AuthAccount) {\\n                log(acct)\\n                log(a)\\n                log(b)\\n              }\\n            }\\n          \",\"args\":[{\"type\":\"String\",\"value\":\"Hello\"},{\"type\":\"String\",\"value\":\"WalletConnect\"}],\"interaction\":{\"tag\":\"TRANSACTION\",\"assigns\":{},\"status\":\"OK\",\"reason\":null,\"accounts\":{\"c60b85621495cfac-0\":{\"kind\":\"ACCOUNT\",\"tempId\":\"c60b85621495cfac-0\",\"addr\":\"c60b85621495cfac\",\"keyId\":0,\"sequenceNum\":21,\"signature\":null,\"resolve\":null,\"role\":{\"proposer\":true,\"authorizer\":true,\"payer\":true,\"param\":false}}},\"params\":{},\"arguments\":{\"0h4pfamnyv\":{\"kind\":\"ARGUMENT\",\"tempId\":\"0h4pfamnyv\",\"value\":\"Hello\",\"asArgument\":{\"type\":\"String\",\"value\":\"Hello\"},\"xform\":{\"label\":\"String\"}},\"emvph2davh\":{\"kind\":\"ARGUMENT\",\"tempId\":\"emvph2davh\",\"value\":\"WalletConnect\",\"asArgument\":{\"type\":\"String\",\"value\":\"WalletConnect\"},\"xform\":{\"label\":\"String\"}}},\"template\":null,\"message\":{\"cadence\":\"\\n            transaction(a: String, b: String) {\\n              prepare(acct: AuthAccount) {\\n                log(acct)\\n                log(a)\\n                log(b)\\n              }\\n            }\\n          \",\"refBlock\":\"7fafbea3a826b777247a14d4b8b950f6d7a84f2be8eba0214a282ceda5240ab2\",\"computeLimit\":999,\"proposer\":null,\"payer\":null,\"authorizations\":[],\"params\":[],\"arguments\":[\"0h4pfamnyv\",\"emvph2davh\"]},\"proposer\":\"c60b85621495cfac-0\",\"authorizations\":[\"c60b85621495cfac-0\"],\"payer\":[\"c60b85621495cfac-0\"],\"events\":{\"eventType\":null,\"start\":null,\"end\":null,\"blockIds\":[]},\"transaction\":{\"id\":null},\"block\":{\"id\":null,\"height\":null,\"isSealed\":null},\"account\":{\"addr\":null},\"collection\":{\"id\":null}},\"voucher\":{\"cadence\":\"\\n            transaction(a: String, b: String) {\\n              prepare(acct: AuthAccount) {\\n                log(acct)\\n                log(a)\\n                log(b)\\n              }\\n            }\\n          \",\"refBlock\":\"7fafbea3a826b777247a14d4b8b950f6d7a84f2be8eba0214a282ceda5240ab2\",\"computeLimit\":999,\"template\":null,\"arguments\":[{\"type\":\"String\",\"value\":\"Hello\"},{\"type\":\"String\",\"value\":\"WalletConnect\"}],\"proposalKey\":{\"address\":\"0xc60b85621495cfac\",\"keyId\":0,\"sequenceNum\":21},\"payer\":\"0xc60b85621495cfac\",\"authorizers\":[\"0xc60b85621495cfac\"],\"payloadSigs\":[],\"envelopeSigs\":[{\"address\":\"0xc60b85621495cfac\",\"keyId\":0,\"sig\":null}]}}"]
        """.trimIndent()

        @Language("JSON")
        val paramsJson = """
            [
              {
                "f_type":"Signable",
                "f_vsn":"1.0.1",
                "message":"464c4f572d56302e302d7472616e73616374696f6e0000000000000000000000f90162f9015eb8cc0a2020202020202020202020207472616e73616374696f6e28613a20537472696e672c20623a20537472696e6729207b0a20202020202020202020202020207072657061726528616363743a20417574684163636f756e7429207b0a202020202020202020202020202020206c6f672861636374290a202020202020202020202020202020206c6f672861290a202020202020202020202020202020206c6f672862290a20202020202020202020202020207d0a2020202020202020202020207d0a20202020202020202020f84ca17b2274797065223a22537472696e67222c2276616c7565223a2248656c6c6f227da97b2274797065223a22537472696e67222c2276616c7565223a2257616c6c6574436f6e6e656374227da07fafbea3a826b777247a14d4b8b950f6d7a84f2be8eba0214a282ceda5240ab28203e788c60b85621495cfac801588c60b85621495cfacc988c60b85621495cfacc0",
                "addr":"0xc60b85621495cfac",
                "keyId":0,
                "roles":{
                  "proposer":true,
                  "authorizer":true,
                  "payer":true,
                  "param":false
                },
                "cadence":"\n            transaction(a: String, b: String) {\n              prepare(acct: AuthAccount) {\n                log(acct)\n                log(a)\n                log(b)\n              }\n            }\n          ",
                "args":[
                  {
                    "type":"String",
                    "value":"Hello"
                  },
                  {
                    "type":"String",
                    "value":"WalletConnect"
                  }
                ],
                "interaction":{
                  "tag":"TRANSACTION",
                  "assigns":{},
                  "status":"OK",
                  "reason":null,
                  "accounts":{
                    "c60b85621495cfac-0":{
                      "kind":"ACCOUNT",
                      "tempId":"c60b85621495cfac-0",
                      "addr":"c60b85621495cfac",
                      "keyId":0,
                      "sequenceNum":21,
                      "signature":null,
                      "resolve":null,
                      "role":{
                        "proposer":true,
                        "authorizer":true,
                        "payer":true,
                        "param":false
                      }
                    }
                  },
                  "params":{},
                  "arguments":{
                    "0h4pfamnyv":{
                      "kind":"ARGUMENT",
                      "tempId":"0h4pfamnyv",
                      "value":"Hello",
                      "asArgument":{
                        "type":"String",
                        "value":"Hello"
                      },
                      "xform":{
                        "label":"String"
                      }
                    },
                    "emvph2davh":{
                      "kind":"ARGUMENT",
                      "tempId":"emvph2davh",
                      "value":"WalletConnect",
                      "asArgument":{
                        "type":"String",
                        "value":"WalletConnect"
                      },
                      "xform":{
                        "label":"String"
                      }
                    }
                  },
                  "template":null,
                  "message":{
                    "cadence":"\n            transaction(a: String, b: String) {\n              prepare(acct: AuthAccount) {\n                log(acct)\n                log(a)\n                log(b)\n              }\n            }\n          ",
                    "refBlock":"7fafbea3a826b777247a14d4b8b950f6d7a84f2be8eba0214a282ceda5240ab2",
                    "computeLimit":999,
                    "proposer":null,
                    "payer":null,
                    "authorizations":[],
                    "params":[],
                    "arguments":["0h4pfamnyv","emvph2davh"]
                  },
                  "proposer":"c60b85621495cfac-0",
                  "authorizations":["c60b85621495cfac-0"],
                  "payer":["c60b85621495cfac-0"],
                  "events":{
                    "eventType":null,
                    "start":null,
                    "end":null,
                    "blockIds":[]
                  },
                  "transaction":{
                    "id":null
                  },
                  "block":{
                    "id":null,
                    "height":null,
                    "isSealed":null
                  },
                  "account":{
                    "addr":null
                  },
                  "collection":{
                    "id":null
                  }
                },
                "voucher":{
                  "cadence":"\n            transaction(a: String, b: String) {\n              prepare(acct: AuthAccount) {\n                log(acct)\n                log(a)\n                log(b)\n              }\n            }\n          ",
                  "refBlock":"7fafbea3a826b777247a14d4b8b950f6d7a84f2be8eba0214a282ceda5240ab2",
                  "computeLimit":999,
                  "template":null,
                  "arguments":[
                    {
                      "type":"String",
                      "value":"Hello"
                    },
                    {
                      "type":"String",
                      "value":"WalletConnect"
                    }
                  ],
                  "proposalKey":{
                    "address":"0xc60b85621495cfac",
                    "keyId":0,
                    "sequenceNum":21
                  },
                  "payer":"0xc60b85621495cfac",
                  "authorizers":["0xc60b85621495cfac"],
                  "payloadSigs":[],
                  "envelopeSigs":[
                    {
                      "address":"0xc60b85621495cfac",
                      "keyId":0,
                      "sig":null
                    }
                  ]
                }
              }
            ]
        """.trimIndent()

        val expectedParamsJsonArray = JSONArray(paramsJson)
        val actualParamsJsonArray = JSONArray(serializedParams)

        assertEquals(expectedParamsJsonArray.length(), actualParamsJsonArray.length())

        iterateJsonArrays(expectedParamsJsonArray, actualParamsJsonArray)
    }

    @Test
    fun jsonObjectWithLongId() {
        params = """
            {
              "id": 1661243868217,
              "jsonrpc": "2.0",
              "result": {
                "f_type": "PollingResponse",
                "status": "APPROVED",
                "f_vsn": "1.0.0",
                "data": {
                  "fVsn": "1.0.0",
                  "paddr": null,
                  "services": [
                    {
                      "f_type": "Service",
                      "uid": "flow-wallet#authn",
                      "provider": {
                        "f_type": "ServiceProvider",
                        "f_vsn": "1.0.0",
                        "name": "Flow Wallet",
                        "address": "8b26e42898e53610"
                      },
                      "id": "8b26e42898e53610",
                      "f_vsn": "1.0.0",
                      "endpoint": "flow_authn",
                      "type": "authn",
                      "identity": { "address": "8b26e42898e53610", "keyId": 0 }
                    },
                    {
                      "f_type": "Service",
                      "method": "WC/RPC",
                      "uid": "flow-wallet#authz",
                      "f_vsn": "1.0.0",
                      "endpoint": "flow_authz",
                      "type": "authz",
                      "identity": { "address": "8b26e42898e53610", "keyId": 0 }
                    },
                    {
                      "f_type": "Service",
                      "method": "WC/RPC",
                      "uid": "flow-wallet#user-signature",
                      "f_vsn": "1.0.0",
                      "endpoint": "flow_user_sign",
                      "type": "user-signature",
                      "identity": { "address": "8b26e42898e53610", "keyId": 0 }
                    }
                  ],
                  "addr": "0x8b26e42898e53610",
                  "fType": "AuthnResponse"
                }
              }
            }
        """.trimIndent()

        val expectedParamsJsonObj = JSONObject(params)
        val actualParamsJsonObj = JSONObject(serializedParams)

        assertEquals(expectedParamsJsonObj.length(), actualParamsJsonObj.length())

        iterateJsonObjects(expectedParamsJsonObj, actualParamsJsonObj)
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