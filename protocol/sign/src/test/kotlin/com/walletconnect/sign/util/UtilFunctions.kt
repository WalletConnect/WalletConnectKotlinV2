package com.walletconnect.sign.util

import junit.framework.TestCase
import org.json.JSONArray
import org.json.JSONObject
internal fun iterateJsonArrays(expJsonArray: JSONArray, actJsonArray: JSONArray) {
    TestCase.assertEquals(expJsonArray.length(), actJsonArray.length())

    (0 until expJsonArray.length()).forEach { index ->
        val expCurrentIndexItem = expJsonArray.get(index)
        val actCurrentIndexItem = actJsonArray[index]

        when {
            expCurrentIndexItem is JSONObject && actCurrentIndexItem is JSONObject -> iterateJsonObjects(expCurrentIndexItem, actCurrentIndexItem)
            expCurrentIndexItem is JSONArray && actCurrentIndexItem is JSONArray -> iterateJsonArrays(expCurrentIndexItem, actCurrentIndexItem)
            else -> TestCase.assertEquals(expCurrentIndexItem, actCurrentIndexItem)
        }
    }
}

internal fun iterateJsonObjects(expJsonObject: JSONObject, actJsonObject: JSONObject) {
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

            else -> TestCase.assertEquals(expCurrentItem, actCurrentItem)
        }
    }
}