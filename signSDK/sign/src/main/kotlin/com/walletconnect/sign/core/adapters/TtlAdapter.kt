package com.walletconnect.sign.core.adapters

import com.squareup.moshi.*
import com.walletconnect.sign.core.model.vo.TtlVO

internal object TtlAdapter : JsonAdapter<TtlVO>() {

    @JvmSynthetic
    @FromJson
    @Qualifier
    override fun fromJson(reader: JsonReader): TtlVO? {
        reader.isLenient = true
        var seconds: Long? = null

        if (reader.hasNext() && reader.peek() == JsonReader.Token.NUMBER) {
            seconds = reader.nextLong()
        }

        return if (seconds != null) {
            TtlVO(seconds)
        } else {
            null
        }
    }

    @JvmSynthetic
    @ToJson
    override fun toJson(writer: JsonWriter, @Qualifier value: TtlVO?) {
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