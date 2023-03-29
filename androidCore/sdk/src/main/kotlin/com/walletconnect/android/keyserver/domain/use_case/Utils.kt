package com.walletconnect.android.keyserver.domain.use_case

import com.walletconnect.android.keyserver.model.KeyServerHttpResponse
import com.walletconnect.android.keyserver.model.KeyServerHttpResponse.Companion.SUCCESS_STATUS
import com.walletconnect.android.keyserver.model.KeyServerResponse
import retrofit2.Response

@JvmSynthetic
internal fun <K, T : KeyServerHttpResponse<K>> Response<T>.unwrapUnit() {
    if (isSuccessful && body() != null) {
        if (body()!!.status == SUCCESS_STATUS) {
            return
        } else {
            throw Throwable(body()!!.error?.message)
        }
    } else {
        throw Throwable(errorBody()?.string())
    }
}

@JvmSynthetic
internal fun <K : KeyServerResponse, T : KeyServerHttpResponse<K>> Response<T>.unwrapValue(): K {
    if (isSuccessful && body() != null) {
        if (body()!!.status == SUCCESS_STATUS) {
            if (body()!!.value != null) {
                return body()!!.value!!
            } else {
                throw Throwable("Expected value is null")
            }
        } else {
            throw Throwable(body()!!.error?.message)
        }
    } else {
        throw Throwable(errorBody()?.string())
    }
}