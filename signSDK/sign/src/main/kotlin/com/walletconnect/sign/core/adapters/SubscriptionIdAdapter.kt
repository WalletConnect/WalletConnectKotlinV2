package com.walletconnect.sign.core.adapters

import com.squareup.moshi.*
import com.walletconnect.sign.core.model.vo.SubscriptionIdVO

internal object SubscriptionIdAdapter: JsonAdapter<SubscriptionIdVO>() {

    @JvmSynthetic
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

    @JvmSynthetic
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