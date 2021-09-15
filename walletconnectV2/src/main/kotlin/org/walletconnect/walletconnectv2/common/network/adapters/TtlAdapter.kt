package org.walletconnect.walletconnectv2.common.network.adapters

import com.squareup.moshi.*
import org.walletconnect.walletconnectv2.common.Ttl

object TtlAdapter: JsonAdapter<Ttl>() {

    @FromJson
    @Qualifier
    override fun fromJson(reader: JsonReader): Ttl? {
        return null
    }

    @ToJson
    override fun toJson(writer: JsonWriter, @Qualifier value: Ttl?) {
        if (value != null) {
            writer.value(value.seconds)
        } else {
            writer.value(0)
        }
    }

    @Retention(AnnotationRetention.RUNTIME)
    @JsonQualifier
    annotation class Qualifier
}