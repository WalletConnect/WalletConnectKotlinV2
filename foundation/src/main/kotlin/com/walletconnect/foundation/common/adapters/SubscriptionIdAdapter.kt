package com.walletconnect.foundation.common.adapters

import com.squareup.moshi.*
import com.walletconnect.foundation.common.model.SubscriptionId

internal object SubscriptionIdAdapter: JsonAdapter<SubscriptionId>() {

    @JvmSynthetic
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

    @JvmSynthetic
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