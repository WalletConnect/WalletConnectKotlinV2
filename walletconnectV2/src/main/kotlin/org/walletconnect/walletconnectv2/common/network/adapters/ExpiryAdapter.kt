package org.walletconnect.walletconnectv2.common.network.adapters

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.ToJson
import org.walletconnect.walletconnectv2.common.Expiry

object ExpiryAdapter {

    @FromJson
    fun fromJson(reader: JsonReader): Expiry? {
        return null
    }

    @ToJson
    fun toJson(writer: JsonWriter, value: Expiry?) {
        if (value != null) {
            writer.value(value.seconds)
        } else {
            writer.value(0)
        }
    }
}