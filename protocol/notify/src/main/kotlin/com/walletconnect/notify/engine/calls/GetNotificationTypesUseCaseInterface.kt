@file:JvmSynthetic

package com.walletconnect.notify.engine.calls

internal class GetNotificationTypesUseCase: GetNotificationTypesUseCaseInterface {
    override suspend fun getNotificationTypes(domain: String, onSuccess: () -> Unit, onError: (Throwable) -> Unit) {
    }
}

internal interface GetNotificationTypesUseCaseInterface {
    suspend fun getNotificationTypes(domain: String, onSuccess: () -> Unit, onError: (Throwable) -> Unit)
}