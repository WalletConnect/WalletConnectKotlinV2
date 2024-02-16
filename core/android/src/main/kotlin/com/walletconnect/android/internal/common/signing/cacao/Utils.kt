package com.walletconnect.android.internal.common.signing.cacao

import com.walletconnect.android.internal.common.signing.cacao.Cacao.Payload.Companion.RECAPS_PREFIX
import com.walletconnect.utils.HexPrefix
import org.bouncycastle.util.encoders.Base64
import org.json.JSONObject

@JvmSynthetic
internal fun String.guaranteeNoHexPrefix(): String = removePrefix(String.HexPrefix)

@JvmSynthetic
fun List<String>?.parseReCaps(): MutableMap<String, MutableMap<String, MutableList<String>>> {
    if (this.isNullOrEmpty()) return emptyMap<String, MutableMap<String, MutableList<String>>>().toMutableMap()
    val reCapsMap: MutableMap<String, MutableMap<String, MutableList<String>>> = mutableMapOf()
    this.forEach { jsonString ->
        val jsonObject = JSONObject(jsonString)
        val attObject = jsonObject.getJSONObject("att")

        attObject.keys().forEach { key ->
            val innerObject = attObject.getJSONObject(key)
            val requestsMap = mutableMapOf<String, MutableList<String>>()

            innerObject.keys().forEach { requestType ->
                val requestArray = innerObject.getJSONArray(requestType)
                val dynamicList = mutableListOf<String>()

                for (i in 0 until requestArray.length()) {
                    val itemObject = requestArray.getJSONObject(i)
                    // Assuming the structure under each requestType contains arrays of strings
                    itemObject.keys().forEach { dynamicKey ->
                        val dynamicArray = itemObject.getJSONArray(dynamicKey)
                        for (j in 0 until dynamicArray.length()) {
                            dynamicList.add(dynamicArray.getString(j))
                        }
                    }
                }

                requestsMap[requestType] = dynamicList
            }

            reCapsMap[key] = requestsMap
        }
    }

    return reCapsMap.mapValues { entry -> entry.value.toMutableMap() }.toMutableMap().toSortedMap()
}

@JvmSynthetic
fun List<String>?.decodeReCaps(): List<String>? {
    return this
        ?.filter { resource -> resource.startsWith(RECAPS_PREFIX) }
        ?.map { urn -> urn.removePrefix(RECAPS_PREFIX) }
        ?.map { encodedReCaps -> Base64.decode(encodedReCaps).toString(Charsets.UTF_8) }
}

@JvmSynthetic
fun List<String>?.getMethods(): List<String> {
    return this.decodeReCaps().parseReCaps()["eip155"]?.keys?.map { key -> key.substringAfter('/') } ?: emptyList()
}

@JvmSynthetic
fun List<String>?.getChains(): List<String> {
    return this.decodeReCaps().parseReCaps()["eip155"]?.values?.flatten()?.distinct() ?: emptyList()
}