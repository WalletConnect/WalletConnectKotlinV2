package com.walletconnect.android.internal.common.signing.cacao

import com.walletconnect.android.internal.common.signing.cacao.Cacao.Payload.Companion.RECAPS_PREFIX
import com.walletconnect.utils.HexPrefix
import org.json.JSONObject

@JvmSynthetic
internal fun String.guaranteeNoHexPrefix(): String = removePrefix(String.HexPrefix)

@JvmSynthetic
fun String?.parseReCaps(): MutableMap<String, MutableMap<String, MutableList<String>>> {
    if (this.isNullOrEmpty()) return emptyMap<String, MutableMap<String, MutableList<String>>>().toMutableMap()
    val reCapsMap: MutableMap<String, MutableMap<String, MutableList<String>>> = mutableMapOf()

    val jsonObject = JSONObject(this)
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

    return reCapsMap.mapValues { entry -> entry.value.toMutableMap() }.toMutableMap()
}

@JvmSynthetic
fun List<String>?.decodeReCaps(): String? {
    return try {
        this
            ?.last { resource -> resource.startsWith(RECAPS_PREFIX) }
            ?.removePrefix(RECAPS_PREFIX)
            .run { java.util.Base64.getDecoder().decode(this).toString(Charsets.UTF_8) }
    } catch (e: Exception) {
        null
    }
}

@JvmSynthetic
fun List<String>?.getMethods(): List<String> {
    return this.decodeReCaps().parseReCaps()["eip155"]?.keys?.sorted()?.map { key -> key.substringAfter('/') } ?: emptyList()
}

@JvmSynthetic
fun List<String>?.getChains(): List<String> {
    return this.decodeReCaps().parseReCaps()["eip155"]?.values?.flatten()?.distinct() ?: emptyList()
}