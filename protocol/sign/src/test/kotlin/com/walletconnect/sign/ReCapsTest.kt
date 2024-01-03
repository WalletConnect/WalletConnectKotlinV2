package com.walletconnect.sign

import org.json.JSONObject
import org.junit.Test
import java.util.Base64

class ReCapsTest {

    private val encodedRecaps = "urn:recap:eyJhdHQiOnsiZWlwMTU1IjpbeyJyZXF1ZXN0L2V0aF9zaWduVHlwZWREYXRhX3Y0IjpbXX0seyJyZXF1ZXN0L3BlcnNvbmFsX3NpZ24iOltdfV19fQ=="

    @Test
    fun encodeReCapsBase64() {
        val recaps =
            JSONObject().put(
                "att",
                JSONObject().put(
                    "eip155", listOf(
                        JSONObject()
                            .put("request/eth_signTypedData_v4", listOf<String>()),
                        JSONObject()
                            .put("request/personal_sign", listOf<String>())
                    )
                )
            )
        print(recaps.toString())
        val base64Recaps = Base64.getEncoder().encodeToString(recaps.toString().toByteArray(Charsets.UTF_8))
        val reCapsUrl = "urn:recap:$base64Recaps"

        assert(reCapsUrl == encodedRecaps)
    }

    @Test
    fun decodeReCapsBase64() {
        val withoutPrefix = encodedRecaps.removePrefix("urn:recap:")
        val reCaps = Base64.getDecoder().decode(withoutPrefix).toString(Charsets.UTF_8)
        val requests = (JSONObject(reCaps).get("att") as JSONObject).getJSONArray("eip155")

        assert((requests.getJSONObject(0).keys().next() as String).split("/")[1] == "eth_signTypedData_v4")
        assert((requests.getJSONObject(1).keys().next() as String).split("/")[1] == "personal_sign")
    }

}

