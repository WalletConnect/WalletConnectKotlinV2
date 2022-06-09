package com.walletconnect.walletconnectv2.core.adapters

import com.squareup.moshi.*
import okio.Buffer
import org.json.JSONException
import org.json.JSONObject

internal object JSONObjectAdapter: JsonAdapter<JSONObject>() {

    @JvmSynthetic
    @FromJson
    @Qualifier
    override fun fromJson(reader: JsonReader): JSONObject? {
        // Here we're expecting the JSON object, it is processed as Map<String, Any> by Moshi
        return (reader.readJsonValue() as? Map<String, Any>)?.let { data ->
            try {
                JSONObject(data)
            } catch (e: JSONException) {
                // Handle error if arises
                JSONObject("")
            }
        }
    }

    @JvmSynthetic
    @ToJson
    override fun toJson(writer: JsonWriter, @Qualifier value: JSONObject?) {
        value?.let { writer.value(Buffer().writeUtf8(value.toString())) }
    }

    @Retention(AnnotationRetention.RUNTIME)
    @JsonQualifier
    annotation class Qualifier
}
