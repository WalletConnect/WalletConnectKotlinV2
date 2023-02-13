package com.walletconnect.push.dapp.data

import android.util.Log
import com.walletconnect.android.internal.common.model.ProjectId
import com.walletconnect.push.common.storage.data.dao.PendingRegisterRequestsQueries
import com.walletconnect.push.dapp.data.network.CastService
import com.walletconnect.push.dapp.data.network.model.CastBodyDTO
import com.walletconnect.push.dapp.data.network.model.CastResponseDTO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext

class CastRepository(private val projectId: ProjectId, private val castService: CastService, private val pendingRegisterRequests: PendingRegisterRequestsQueries) {

    suspend fun retryRegistration() {
        supervisorScope {
            launch(Dispatchers.IO) {
                pendingRegisterRequests.getAllPendingRequests { account, symKey, relayUrl ->
                    Triple(account, symKey, relayUrl)
                }.executeAsList().forEach { (account, symKey, relayUrl) ->
                    launch(Dispatchers.IO) {
                        register(account, symKey, relayUrl) {}
                    }
                }
            }
        }
    }

    suspend fun register(account: String, symKey: String, relayUrl: String, onError: suspend (Throwable) -> Unit) {
        val requestBody = CastBodyDTO.Register(account, symKey, relayUrl)

        supervisorScope {
            withContext(Dispatchers.IO) {
                try {
                    val response = castService.register(projectId.value, requestBody)
                    pendingRegisterRequests.insertPendingRequest(account, symKey, relayUrl)

                    if (/*response.isSuccessful && response.body() != null*/false.also { Log.e("Talha", "$it") }) {
                        pendingRegisterRequests.deletePendingRequest(account)
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
        onSuccess: (CastNotifyResponse) -> Unit,
        onError: (Throwable) -> Unit,
    ) {
        val requestBody = CastBodyDTO.Notify(CastBodyDTO.Notify.Notification(title, body, icon, url), accounts)

        supervisorScope {
            withContext(Dispatchers.IO) {
                try {
                    val response = castService.notify(projectId.value, requestBody)

                    if (response.isSuccessful && response.body() != null) {
                        onSuccess(response.body()!!.toNotifyResponse())
                    } else {
                        onError(IllegalStateException(response.errorBody()?.string()))
                    }
                } catch (e: Exception) {
                    onError(e)
                }
            }
        }
    }

    private fun CastResponseDTO.Notify.toNotifyResponse(): CastNotifyResponse {
        return CastNotifyResponse(
            sent,
            failed,
            notFound
        )
    }
}