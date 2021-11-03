package org.walletconnect.walletconnectv2.common.network.adapters

import com.squareup.moshi.*
import org.walletconnect.walletconnectv2.common.Expiry

object ExpiryAdapter: JsonAdapter<Expiry>() {

    @FromJson
    @Qualifier
    override fun fromJson(reader: JsonReader): Expiry? {
        return null
    }

    @ToJson
    override fun toJson(writer: JsonWriter, @Qualifier value: Expiry?) {
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