package com.walletconnect.sign

import org.bouncycastle.util.encoders.Base64
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Test


/*
*   func testRecapNotifyAndSign() throws {
        let notifyRecapJson = """
        {
           "att":{
              "https://notify.walletconnect.com/all-apps":{
                 "crud/notifications": [{}],
                 "crud/subscriptions": [{}]
              }
           }
        }
        """

        let signRecapJson = """
        {
           "att":{
              "eip155":{
                 "request/eth_sendTransaction": [{}],
                 "request/personal_sign": [{}]
              }
           }
        }
        """

        // Correctly constructing Data from JSON strings
        guard let notifyRecapData = notifyRecapJson.data(using: .utf8),
              let signRecapData = signRecapJson.data(using: .utf8) else {
            XCTFail("Failed to create Data from JSON strings")
            return
        }

        let encodedNotify = notifyRecapData.base64EncodedString()
        let encodedSign = signRecapData.base64EncodedString()

        let urn1 = try RecapUrn(urn: "urn:recap:\(encodedSign)")
        let urn2 = try RecapUrn(urn: "urn:recap:\(encodedNotify)")


        let expectedStatement = """
        I further authorize the stated URI to perform the following actions on my behalf: (1) 'request': 'eth_sendTransaction', 'personal_sign' for 'eip155'. (2) 'crud': 'notifications', 'subscriptions' for 'https://notify.walletconnect.com/all-apps'.
        """
* */

class ReCapsTest {

    private val encodedSignRecaps = "urn:recap:eyJhdHQiOnsiZWlwMTU1Ijp7InJlcXVlc3QvcGVyc29uYWxfc2lnbiI6W3t9XSwicmVxdWVzdC9ldGhfc2lnblR5cGVkRGF0YV92NCI6W3t9XX19fQ=="
    private val encodedNotifyRecaps = "urn:recap:eyJhdHQiOnsiaHR0cHM6Ly9ub3RpZnkud2FsbGV0Y29ubmVjdC5jb20vYWxsLWFwcHMiOnsiY3J1ZC9zdWJzY3JpcHRpb25zIjpbe31dLCJjcnVkL25vdGlmaWNhdGlvbnMiOlt7fV19fX0="
    private val encodedNotifyAndSignRecaps = "urn:recap:eyJhdHQiOnsiZWlwMTU1Ijp7InJlcXVlc3QvcGVyc29uYWxfc2lnbiI6W3t9XSwicmVxdWVzdC9ldGhfc2lnblR5cGVkRGF0YV92NCI6W3t9XX0sImh0dHBzOi8vbm90aWZ5LndhbGxldGNvbm5lY3QuY29tL2FsbC1hcHBzIjp7ImNydWQvc3Vic2NyaXB0aW9ucyI6W3t9XSwiY3J1ZC9ub3RpZmljYXRpb25zIjpbe31dfX19"

    @Test
    fun encodeSignReCapsBase64() {
        val signReCapsJson =
            JSONObject().put(
                "att",
                JSONObject().put(
                    "eip155",
                    JSONObject()
                        .put("request/eth_signTypedData_v4", JSONArray().put(0, JSONObject()))
                        .put("request/personal_sign", JSONArray().put(0, JSONObject()))
                )
            )
        val base64Recaps = Base64.toBase64String(signReCapsJson.toString().toByteArray(Charsets.UTF_8))
        val reCapsUrl = "urn:recap:$base64Recaps"

        assert(reCapsUrl == encodedSignRecaps)
    }

    @Test
    fun decodeSignReCapsBase64() {
        val withoutPrefix = encodedSignRecaps.removePrefix("urn:recap:")
        val reCaps = Base64.decode(withoutPrefix).toString(Charsets.UTF_8)
        val actions = (JSONObject(reCaps).get("att") as JSONObject).getJSONObject("eip155")

        assert((actions.keys().asSequence().toList()[0]).split("/")[1] == "personal_sign")
        assert((actions.keys().asSequence().toList()[1]).split("/")[1] == "eth_signTypedData_v4")
    }

    @Test
    fun encodeNotifyReCapsBase64() {
        val notifyReCapsJson =
            JSONObject().put(
                "att",
                JSONObject().put(
                    "https://notify.walletconnect.com/all-apps",
                    JSONObject()
                        .put("crud/notifications", JSONArray().put(0, JSONObject()))
                        .put("crud/subscriptions", JSONArray().put(0, JSONObject()))
                )
            )
        val base64Recaps = Base64.toBase64String(notifyReCapsJson.toString().toByteArray(Charsets.UTF_8))
        val reCapsUrl = "urn:recap:$base64Recaps"

        assert(reCapsUrl == encodedNotifyRecaps)
    }

    @Test
    fun decodeNotifyReCapsBase64() {
        val withoutPrefix = encodedNotifyRecaps.removePrefix("urn:recap:")
        val reCaps = Base64.decode(withoutPrefix).toString(Charsets.UTF_8)
        val actions = (JSONObject(reCaps).get("att") as JSONObject).getJSONObject("https://notify.walletconnect.com/all-apps")

        assert((actions.keys().asSequence().toList()[0]).split("/")[1] == "subscriptions")
        assert((actions.keys().asSequence().toList()[1]).split("/")[1] == "notifications")
    }

    @Test
    fun encodeNotifyAndSignReCapsBase64() {
        val notifyReCapsJson =
            JSONObject().put(
                "att",
                JSONObject()
                    .put(
                        "https://notify.walletconnect.com/all-apps",
                        JSONObject()
                            .put("crud/notifications", JSONArray().put(0, JSONObject()))
                            .put("crud/subscriptions", JSONArray().put(0, JSONObject()))
                    )
                    .put(
                        "eip155",
                        JSONObject()
                            .put("request/eth_signTypedData_v4", JSONArray().put(0, JSONObject()))
                            .put("request/personal_sign", JSONArray().put(0, JSONObject()))
                    ),
            )
        println(notifyReCapsJson.toString())
        val base64Recaps = Base64.toBase64String(notifyReCapsJson.toString().toByteArray(Charsets.UTF_8))
        val reCapsUrl = "urn:recap:$base64Recaps"

        assert(reCapsUrl == encodedNotifyAndSignRecaps)
    }

    @Test
    fun decodeNotifyAndSignReCapsBase64() {
        val withoutPrefix = encodedNotifyAndSignRecaps.removePrefix("urn:recap:")
        val reCaps = Base64.decode(withoutPrefix).toString(Charsets.UTF_8)
        val notifyActions = (JSONObject(reCaps).get("att") as JSONObject).getJSONObject("https://notify.walletconnect.com/all-apps")
        val signActions = (JSONObject(reCaps).get("att") as JSONObject).getJSONObject("eip155")

        assert((notifyActions.keys().asSequence().toList()[0]).split("/")[1] == "subscriptions")
        assert((notifyActions.keys().asSequence().toList()[1]).split("/")[1] == "notifications")

        assert((signActions.keys().asSequence().toList()[0]).split("/")[1] == "personal_sign")
        assert((signActions.keys().asSequence().toList()[1]).split("/")[1] == "eth_signTypedData_v4")
    }
}

