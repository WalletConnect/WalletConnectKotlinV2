package org.walletconnect.walletconnectv2.common.network.adapters

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.ToJson
import org.walletconnect.walletconnectv2.common.Ttl

object TtlAdapter {

    @FromJson
    fun fromJson(reader: JsonReader): Ttl? {
        return null
    }

    @ToJson
    fun toJson(writer: JsonWriter, value: Ttl?) {
        if (value != null) {
            writer.value(value.seconds)
        } else {
            writer.value(0)
        }
    }
}