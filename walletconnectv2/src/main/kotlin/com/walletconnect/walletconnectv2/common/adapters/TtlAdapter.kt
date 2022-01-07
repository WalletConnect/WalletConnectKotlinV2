package com.walletconnect.walletconnectv2.common.adapters

import com.squareup.moshi.*
import com.walletconnect.walletconnectv2.common.model.vo.TtlVO

object TtlAdapter : JsonAdapter<TtlVO>() {

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