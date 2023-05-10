package com.walletconnect.web3.modal.domain.configuration

import android.net.Uri
import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonQualifier
import com.squareup.moshi.ToJson
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory


@Retention(AnnotationRetention.RUNTIME)
@JsonQualifier
internal annotation class EncodedString

internal class EncodedStringAdapter {

    @EncodedString
    @FromJson
    fun fromJson(json: String): String {
        return Uri.decode(json)
    }

    @ToJson
    fun toJson(@EncodedString value: String): String {
        return Uri.encode(value)
    }
}

internal fun configAdapter(): PolymorphicJsonAdapterFactory<Config> {
    return PolymorphicJsonAdapterFactory.of(Config::class.java, "type")
        .withSubtype(Config.Connect::class.java, "connect")
}
