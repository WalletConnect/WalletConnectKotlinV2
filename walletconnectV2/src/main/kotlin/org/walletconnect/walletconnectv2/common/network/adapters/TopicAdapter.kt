package org.walletconnect.walletconnectv2.common.network.adapters

import com.squareup.moshi.*
import org.walletconnect.walletconnectv2.common.Topic

object TopicAdapter: JsonAdapter<Topic>() {

    @FromJson
    @Qualifier
    override fun fromJson(reader: JsonReader): Topic? {
        return null
    }

    @ToJson
    override fun toJson(writer: JsonWriter, @Qualifier value: Topic?) {
        if (value != null) {
            writer.value(value.topicValue)
        } else {
            writer.value("")
        }
    }

    @Retention(AnnotationRetention.RUNTIME)
    @JsonQualifier
    annotation class Qualifier
}