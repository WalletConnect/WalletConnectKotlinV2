package com.walletconnect.walletconnectv2.common.adapters

import com.squareup.moshi.*
import com.walletconnect.walletconnectv2.common.model.vo.ExpiryVO

internal object ExpiryAdapter: JsonAdapter<ExpiryVO>() {

    @FromJson
    @Qualifier
    override fun fromJson(reader: JsonReader): ExpiryVO? {
        return null
    }

    @ToJson
    override fun toJson(writer: JsonWriter, @Qualifier value: ExpiryVO?) {
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