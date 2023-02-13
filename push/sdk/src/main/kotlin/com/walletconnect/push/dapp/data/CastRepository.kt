package com.walletconnect.push.dapp.network

import com.walletconnect.android.internal.common.model.ProjectId
import com.walletconnect.android.internal.common.scope
import com.walletconnect.push.dapp.network.model.CastBodyDTO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

class CastRepository(private val projectId: ProjectId, private val castService: CastService) {

    suspend fun register(account: String, symKey: String, relayUrl: String, onSuccess: () -> Unit, onError: (Throwable) -> Unit) {
        val body = CastBodyDTO.Register(account, symKey, relayUrl)

        scope.launch(Dispatchers.IO) {
            supervisorScope {
                try {
                    val response = castService.register(projectId.value, body)

                    if (response.isSuccessful && response.body() != null) {
                        onSuccess()
                    } else {
                        onError(IllegalStateException(response.errorBody()?.string()))
                    }
                } catch (e: Exception) {
                    onError(e)
                }
            }
        }
    }

    suspend fun notify(
        title: String,
        body: String,
        icon: String,
        url: String,
        accounts: List<String>,
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit,
    ) {
        val body = CastBodyDTO.Notify(CastBodyDTO.Notify.Notification(title, body, icon, url), accounts)

        scope.launch(Dispatchers.IO) {
            supervisorScope {
                try {
                    val response = castService.notify(projectId.value, body)

                    if (response.isSuccessful && response.body() != null) {
                        if (response.body()!!.notFound)
                    }
                } catch (e: Exception) {
                    onError(e)
                }
            }
        }
    }
}