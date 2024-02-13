package com.walletconnect.sign

import org.bouncycastle.util.encoders.Base64
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Test

class ReCapsTest {
    private val encodedSignRecaps = "urn:recap:eyJhdHQiOnsiZWlwMTU1Ijp7InJlcXVlc3QvcGVyc29uYWxfc2lnbiI6W3t9XSwicmVxdWVzdC9ldGhfc2lnblR5cGVkRGF0YV92NCI6W3t9XX19fQ=="
    private val encodedNotifyRecaps = "urn:recap:eyJhdHQiOnsiaHR0cHM6Ly9ub3RpZnkud2FsbGV0Y29ubmVjdC5jb20vYWxsLWFwcHMiOnsiY3J1ZC9zdWJzY3JpcHRpb25zIjpbe31dLCJjcnVkL25vdGlmaWNhdGlvbnMiOlt7fV19fX0="
    private val encodedNotifyAndSignRecaps =
        "urn:recap:eyJhdHQiOnsiZWlwMTU1Ijp7InJlcXVlc3QvcGVyc29uYWxfc2lnbiI6W3t9XSwicmVxdWVzdC9ldGhfc2lnblR5cGVkRGF0YV92NCI6W3t9XX0sImh0dHBzOi8vbm90aWZ5LndhbGxldGNvbm5lY3QuY29tL2FsbC1hcHBzIjp7ImNydWQvc3Vic2NyaXB0aW9ucyI6W3t9XSwiY3J1ZC9ub3RpZmljYXRpb25zIjpbe31dfX19"

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

        println(signReCapsJson.toString())
        val base64Recaps = Base64.toBase64String(signReCapsJson.toString().toByteArray(Charsets.UTF_8))
        val reCapsUrl = "urn:recap:$base64Recaps"

        assert(reCapsUrl == encodedSignRecaps)
    }

    @Test
    fun decodeSignReCapsBase64() {
        val withoutPrefix = encodedSignRecaps.removePrefix("urn:recap:")
        val reCaps = Base64.decode(withoutPrefix).toString(Charsets.UTF_8)
        val attKeys = JSONObject(reCaps).getJSONObject("att").keys().asSequence().toList()
        val listOfAtts = mutableListOf<JSONObject>()
        attKeys.forEach { key -> listOfAtts.add((JSONObject(reCaps).getJSONObject("att") as JSONObject).getJSONObject(key)) }

        listOfAtts.forEach { att ->
            val actions = att.keys().asSequence().toList()
            assert((actions[0]).split("/")[1] == "personal_sign")
            assert((actions[1]).split("/")[1] == "eth_signTypedData_v4")
        }
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
        val attKeys = JSONObject(reCaps).getJSONObject("att").keys().asSequence().toList()
        val listOfAtts = mutableListOf<JSONObject>()
        attKeys.forEach { key -> listOfAtts.add((JSONObject(reCaps).getJSONObject("att") as JSONObject).getJSONObject(key)) }

        listOfAtts.forEach { att ->
            val actions = att.keys().asSequence().toList()
            assert((actions[0]).split("/")[1] == "subscriptions")
            assert((actions[1]).split("/")[1] == "notifications")
        }
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
        val attKeys = JSONObject(reCaps).getJSONObject("att").keys().asSequence().toList()
        val listOfAtts = mutableListOf<JSONObject>()
        attKeys.forEach { key -> listOfAtts.add((JSONObject(reCaps).getJSONObject("att") as JSONObject).getJSONObject(key)) }

        assert((listOfAtts[0].keys().asSequence().toList()[0]).split("/")[1] == "personal_sign")
        assert((listOfAtts[0].keys().asSequence().toList()[1]).split("/")[1] == "eth_signTypedData_v4")

        assert((listOfAtts[1].keys().asSequence().toList()[0]).split("/")[1] == "subscriptions")
        assert((listOfAtts[1].keys().asSequence().toList()[1]).split("/")[1] == "notifications")
    }
}

