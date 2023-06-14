@file:JvmSynthetic

package com.walletconnect.android.internal.common.adapter

import com.squareup.moshi.*
import com.walletconnect.android.internal.common.model.Expiry

internal object ExpiryAdapter: JsonAdapter<Expiry>() {

    @JvmSynthetic
    @FromJson
    @Qualifier
    override fun fromJson(reader: JsonReader): Expiry? {
        reader.isLenient = true
        var seconds: Long? = null

        if (reader.hasNext() && reader.peek() == JsonReader.Token.NUMBER) {
            seconds = reader.nextLong()
        }

        return if (seconds != null) {
            Expiry(seconds)
        } else {
            null
        }
    }

    @JvmSynthetic
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
    internal annotation class Qualifier
}