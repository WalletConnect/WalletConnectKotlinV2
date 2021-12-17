package com.walletconnect.walletconnectv2.common.network.adapters

import com.squareup.moshi.*
import com.walletconnect.walletconnectv2.common.SubscriptionId

object SubscriptionIdAdapter: JsonAdapter<SubscriptionId>() {

    @FromJson
    @Qualifier
    override fun fromJson(reader: JsonReader): SubscriptionId? {
        reader.isLenient = true
        var subscriptionId: String? = null

        if (reader.hasNext() && reader.peek() == JsonReader.Token.STRING) {
            subscriptionId = reader.nextString()
        }

        return if (subscriptionId != null) {
            SubscriptionId(subscriptionId)
        } else {
            null
        }
    }

    @ToJson
    override fun toJson(writer: JsonWriter, @Qualifier value: SubscriptionId?) {
        if (value != null) {
            writer.value(value.id)
        } else {
            writer.value("")
        }
    }

    @Retention(AnnotationRetention.RUNTIME)
    @JsonQualifier
    annotation class Qualifier
}