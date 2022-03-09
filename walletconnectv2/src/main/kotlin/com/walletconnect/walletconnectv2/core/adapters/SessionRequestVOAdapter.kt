package com.walletconnect.walletconnectv2.core.adapters

import com.squareup.moshi.*
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.session.after.params.SessionRequestVO

internal object SessionRequestVOAdapter: JsonAdapter<SessionRequestVO>() {

    @JvmSynthetic
    @FromJson
    @Qualifier
    override fun fromJson(reader: JsonReader): SessionRequestVO? {
        TODO("Not yet implemented")
    }

    @JvmSynthetic
    @ToJson
    override fun toJson(writer: JsonWriter, value: SessionRequestVO?) {
        TODO("Not yet implemented")
    }

    @Retention(AnnotationRetention.RUNTIME)
    @JsonQualifier
    internal annotation class Qualifier
}