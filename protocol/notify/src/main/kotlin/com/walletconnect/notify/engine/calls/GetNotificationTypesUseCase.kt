@file:JvmSynthetic

package com.walletconnect.notify.engine.calls

internal class GetNotificationTypesUseCase: GetNotificationTypesInterface {
    override suspend fun getNotificationTypes(domain: String, onSuccess: () -> Unit, onError: (Throwable) -> Unit) {
    }
}

internal interface GetNotificationTypesInterface {
    suspend fun getNotificationTypes(domain: String, onSuccess: () -> Unit, onError: (Throwable) -> Unit)
}