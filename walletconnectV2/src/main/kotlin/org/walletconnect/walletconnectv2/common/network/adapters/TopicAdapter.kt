package org.walletconnect.walletconnectv2.common.network.adapters

import com.squareup.moshi.*
import org.walletconnect.walletconnectv2.common.Topic

internal object TopicAdapter {

    @FromJson
    fun fromJson(reader: JsonReader): Topic? {
        return when (val json = reader.readJsonValue()) {
            is String -> Topic(json)
            else -> null
        }
    }

    @ToJson
    fun toJson(writer: JsonWriter, value: Topic?) {
        if (value != null) {
            writer.value(value.topicValue)
        } else {
            writer.value("")
        }
    }
}