@file:JvmSynthetic

package com.walletconnect.android.internal.common.adapter

import com.squareup.moshi.*
import com.walletconnect.android.internal.common.model.Tags

internal object TagsAdapter : JsonAdapter<Tags>() {

    @JvmSynthetic
    @FromJson
    @Qualifier
    override fun fromJson(reader: JsonReader): Tags? {
        reader.isLenient = true
        var id: Int? = null

        if (reader.hasNext() && reader.peek() == JsonReader.Token.NUMBER) {
            id = reader.nextInt()
        }

        return Tags.values().find { it.id == id }
    }

    @JvmSynthetic
    @ToJson
    override fun toJson(writer: JsonWriter, @Qualifier value: Tags?) {
        if (value != null) {
            writer.value(value.id)
        } else {
            writer.value(0)
        }
    }

    @Retention(AnnotationRetention.RUNTIME)
    @JsonQualifier
    annotation class Qualifier
}