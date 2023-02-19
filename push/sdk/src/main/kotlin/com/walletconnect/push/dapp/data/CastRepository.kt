package com.walletconnect.push.dapp.data

import com.walletconnect.android.internal.common.model.ProjectId
import com.walletconnect.push.common.storage.data.dao.PendingRegisterRequestsQueries
import com.walletconnect.push.dapp.data.network.CastService
import com.walletconnect.push.dapp.data.network.model.CastBody
import com.walletconnect.push.dapp.data.network.model.CastResponse
import kotlinx.coroutines.*

internal class CastRepository(private val projectId: ProjectId, private val castService: CastService, private val pendingRegisterRequests: PendingRegisterRequestsQueries) {

    suspend fun retryRegistration() {
        supervisorScope {
            launch(Dispatchers.IO) {
                pendingRegisterRequests.getAllPendingRequests().executeAsList().forEach { (account, symKey, relayUrl, topic) ->
                    coroutineScope {
                        register(account, symKey, relayUrl, topic) {}
                    }
                }
            }
        }
    }

    suspend fun register(account: String, symKey: String, relayUrl: String, topic: String, onError: suspend (Throwable) -> Unit) {
        val requestBody = CastBody.Register(account, symKey, relayUrl)

        supervisorScope {
            withContext(Dispatchers.IO) {
                try {
                    val response = castService.register(projectId.value, requestBody)
                    pendingRegisterRequests.insertPendingRequest(account, symKey, relayUrl, topic)

                    if (response.isSuccessful && response.body() != null) {
                        pendingRegisterRequests.deletePendingRequestByAcount(account)
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
        icon: String?,
        url: String?,
        accounts: List<String>,
        onSuccess: (CastNotifyResponse) -> Unit,
        onError: (Throwable) -> Unit,
    ) {
        val requestBody = CastBody.Notify(CastBody.Notify.Notification(title, body, icon, url), accounts)

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

    suspend fun deletePendingRequest(topic: String) {
        supervisorScope {
            launch(Dispatchers.IO) {
                pendingRegisterRequests.deletePendingRequestByTopic(topic)
            }
        }
    }

    private fun CastResponse.Notify.toNotifyResponse(): CastNotifyResponse {
        return CastNotifyResponse(
            sent,
            failed.map { it.toFailedResponse() },
            notFound
        )
    }

    private fun CastResponse.Notify.Failed.toFailedResponse(): CastNotifyResponse.Failed =
        CastNotifyResponse.Failed(account, reason)
}