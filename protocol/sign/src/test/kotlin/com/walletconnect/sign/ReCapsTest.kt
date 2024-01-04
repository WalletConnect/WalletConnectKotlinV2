package com.walletconnect.sign

import org.bouncycastle.util.encoders.Base64
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Test

class ReCapsTest {

    private val encodedRecaps = "urn:recap:eyJhdHQiOnsiZWlwMTU1IjpbeyJyZXF1ZXN0L2V0aF9zaWduVHlwZWREYXRhX3Y0IjpbXX0seyJyZXF1ZXN0L3BlcnNvbmFsX3NpZ24iOltdfV19fQ=="

    @Test
    fun encodeReCapsBase64() {
        val recaps =
            JSONObject().put(
                "att",
                JSONObject().put(
                    "eip155",
                    JSONArray()
                        .put(
                            0,
                            JSONObject().put("request/eth_signTypedData_v4", JSONArray())
                        )
                        .put(
                            1,
                            JSONObject().put("request/personal_sign", JSONArray())
                        )
                )
            )
        println(recaps.toString())
        val base64Recaps = Base64.toBase64String(recaps.toString().toByteArray(Charsets.UTF_8))
        val reCapsUrl = "urn:recap:$base64Recaps"

        assert(reCapsUrl == encodedRecaps)
    }

    @Test
    fun decodeReCapsBase64() {
        val withoutPrefix = encodedRecaps.removePrefix("urn:recap:")
        val reCaps = Base64.decode(withoutPrefix).toString(Charsets.UTF_8)
        print(reCaps)
        val requests = (JSONObject(reCaps).get("att") as JSONObject).getJSONArray("eip155")

        assert((requests.getJSONObject(0).keys().next() as String).split("/")[1] == "eth_signTypedData_v4")
        assert((requests.getJSONObject(1).keys().next() as String).split("/")[1] == "personal_sign")
    }
}

