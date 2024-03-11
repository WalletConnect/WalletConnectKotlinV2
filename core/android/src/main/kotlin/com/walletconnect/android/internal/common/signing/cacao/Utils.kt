package com.walletconnect.android.internal.common.signing.cacao

import com.walletconnect.android.internal.common.signing.cacao.Cacao.Payload.Companion.RECAPS_PREFIX
import com.walletconnect.utils.HexPrefix
import org.json.JSONArray
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
        val last = this?.last()
        if (last != null && last.startsWith(RECAPS_PREFIX)) {
            java.util.Base64.getDecoder().decode(last.removePrefix(RECAPS_PREFIX)).toString(Charsets.UTF_8)
        } else {
            null
        }
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

@JvmSynthetic
fun mergeReCaps(json1: JSONObject, json2: JSONObject): String {
    val result = JSONObject(json1.toString()) // Start with a deep copy of json1

    json2.keys().forEach { key ->
        if (!result.has(key)) {
            // If json1 does not have the key, simply put the json2 object/array/primitive
            result.put(key, json2.get(key))
        } else {
            // If both json1 and json2 have the object, merge them
            val value1 = result.get(key)
            val value2 = json2.get(key)

            when {
                value1 is JSONObject && value2 is JSONObject -> {
                    result.put(key, mergeReCaps(value1, value2))
                }

                value1 is JSONArray && value2 is JSONArray -> {
                    // Concatenate arrays, respecting ordering rules if specified
                    val mergedArray = concatenateJsonArrays(value1, value2)
                    result.put(key, mergedArray)
                }

                else -> {
                    // For primitive types or if types are different, json2 overrides json1
                    result.put(key, value2)
                }
            }
        }
    }
    return result.toString().replace("\\\"", "\"").replace("\"{", "{").replace("}\"", "}")
}

private fun concatenateJsonArrays(arr1: JSONArray, arr2: JSONArray): JSONArray {
    val result = JSONArray()
    for (i in 0 until arr1.length()) {
        result.put(arr1.get(i))
    }
    for (i in 0 until arr2.length()) {
        result.put(arr2.get(i))
    }
    return result
}