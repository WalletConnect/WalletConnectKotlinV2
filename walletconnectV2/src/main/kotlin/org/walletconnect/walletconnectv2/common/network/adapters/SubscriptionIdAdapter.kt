package org.walletconnect.walletconnectv2.common.network.adapters

import com.squareup.moshi.*
import org.walletconnect.walletconnectv2.common.SubscriptionId
import org.walletconnect.walletconnectv2.util.jsonObject

object SubscriptionIdAdapter: JsonAdapter<SubscriptionId>() {

    @FromJson
    @Qualifier
    override fun fromJson(reader: JsonReader): SubscriptionId? {
        var subscriptionId: String? = null

        reader.jsonObject {
            while (reader.hasNext()) {
                when (reader.peek()) {
                    JsonReader.Token.NAME -> {
                        if (reader.nextName() == "result") {
                            subscriptionId = reader.nextString()
                        } else {
                            reader.skipValue()
                        }
                    }
                    else -> reader.skipValue()
                }
            }
        }

        return if (subscriptionId != null) {
            SubscriptionId(subscriptionId!!)
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