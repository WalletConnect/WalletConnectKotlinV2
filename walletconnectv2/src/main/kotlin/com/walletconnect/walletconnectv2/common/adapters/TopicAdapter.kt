package com.walletconnect.walletconnectv2.common.adapters

import com.squareup.moshi.*
import com.walletconnect.walletconnectv2.common.model.Topic

object TopicAdapter: JsonAdapter<Topic>() {

    @FromJson
    @Qualifier
    override fun fromJson(reader: JsonReader): Topic? {
        reader.isLenient = true
        var topicValue: String? = null

        if (reader.hasNext() && reader.peek() == JsonReader.Token.STRING) {
            topicValue = reader.nextString()
        }

        return if (topicValue != null) {
            Topic(topicValue)
        } else {
            null
        }
    }

    @ToJson
    override fun toJson(writer: JsonWriter, @Qualifier value: Topic?) {
        if (value != null) {
            writer.value(value.value)
        } else {
            writer.value("")
        }
    }

    @Retention(AnnotationRetention.RUNTIME)
    @JsonQualifier
    annotation class Qualifier
}