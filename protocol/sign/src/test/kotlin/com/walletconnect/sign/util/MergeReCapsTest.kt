package com.walletconnect.sign.util

import com.walletconnect.android.internal.common.signing.cacao.mergeReCaps
import org.json.JSONObject
import org.junit.Test

class MergeReCapsTest {

    @Test
    fun `merge two different ReCaps`() {
        val json1 = """
        {
          "att": {
            "eip155": {
              "request/eth_chainId": [{}],
              "request/eth_signTypedData_v4": [{}],
              "request/personal_sign": [{}]
            }
          }
        }
    """.trimIndent()

        val json2 = """
        {
          "att": {
            "https://notify.walletconnect.com/all-apps": { "crud/subscriptions": [{}], "crud/notifications": [{}] }
          }
        }
    """.trimIndent()

        mergeReCaps(JSONObject(json1), JSONObject(json2)).also {
            println(it)
            assert(
                it == """{"att":{"eip155":{"request/personal_sign":[{}],"request/eth_signTypedData_v4":[{}],"request/eth_chainId":[{}]},"https://notify.walletconnect.com/all-apps":{"crud/subscriptions":[{}],"crud/notifications":[{}]}}}"""
            )
        }
    }

    @Test
    fun `merge two ReCaps with overriding fields`() {
        val json1 = """
        {
          "att": {
            "https://example1.com": {
              "crud/read": [{}]
            }
          }
        }
    """.trimIndent()

        val json2 = """
            {
              "att": {
                "https://example1.com": {
                  "crud/update": [{
                    "max_times": 1
                  }]
                },
                "https://example2.com": {
                  "crud/delete": [{}]
                }
              },
            }
    """.trimIndent()

        mergeReCaps(JSONObject(json1), JSONObject(json2)).also {
            println(it)
            assert(
                it == """{"att":{"https://example2.com":{"crud/delete":[{}]},"https://example1.com":{"crud/read":[{}],"crud/update":[{"max_times":1}]}}}""".trimIndent()
            )
        }
    }
}