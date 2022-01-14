package com.walletconnect.walletconnectv2.common.adapters

import com.squareup.moshi.*
import com.walletconnect.walletconnectv2.common.model.vo.SubscriptionIdVO

object SubscriptionIdAdapter: JsonAdapter<SubscriptionIdVO>() {

    @FromJson
    @Qualifier
    override fun fromJson(reader: JsonReader): SubscriptionIdVO? {
        reader.isLenient = true
        var subscriptionId: String? = null

        if (reader.hasNext() && reader.peek() == JsonReader.Token.STRING) {
            subscriptionId = reader.nextString()
        }

        return if (subscriptionId != null) {
            SubscriptionIdVO(subscriptionId)
        } else {
            null
        }
    }

    @ToJson
    override fun toJson(writer: JsonWriter, @Qualifier value: SubscriptionIdVO?) {
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