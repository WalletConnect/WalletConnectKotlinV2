@file:JvmSynthetic

package com.walletconnect.android.echo

import com.walletconnect.android.echo.network.EchoService
import com.walletconnect.android.echo.network.model.EchoBody
import com.walletconnect.android.internal.common.di.AndroidCommonDITags
import com.walletconnect.android.internal.common.model.ProjectId
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.internal.common.wcKoinApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import org.koin.core.qualifier.named

internal object EchoClient : EchoInterface {
    private val echoService by lazy { wcKoinApp.koin.get<EchoService>() }
    private val clientId by lazy { wcKoinApp.koin.get<String>(named(AndroidCommonDITags.CLIENT_ID)) }
    private val projectId by lazy { wcKoinApp.koin.get<ProjectId>() }
    private const val SUCCESS_STATUS = "SUCCESS"

    override fun register(firebaseAccessToken: String, onSuccess: () -> Unit, onError: (Throwable) -> Unit) {
        val body = EchoBody(clientId, firebaseAccessToken)

        scope.launch(Dispatchers.IO) {
            supervisorScope {
                try {
                    val response = echoService.register(projectId.value, body)

                    if (response.isSuccessful && response.body() != null) {
                        if (response.body()!!.status == SUCCESS_STATUS) {
                            onSuccess()
                        } else {
                            onError(IllegalArgumentException(response.body()!!.errors?.first()?.message))
                        }
                    } else {
                        onError(IllegalArgumentException(response.errorBody()?.string()))
                    }
                } catch (e: Exception) {
                    onError(e)
                }
            }
        }
    }

    override fun unregister(onSuccess: () -> Unit, onError: (Throwable) -> Unit) {
        scope.launch(Dispatchers.IO) {
            supervisorScope {
                try {
                    val response = echoService.unregister(projectId.value, clientId)

                    if (response.isSuccessful && response.body() != null) {
                        if (response.body()!!.status == SUCCESS_STATUS) {
                            onSuccess()
                        } else {
                            onError(IllegalArgumentException(response.body()!!.errors?.first()?.message))
                        }
                    } else {
                        onError(IllegalArgumentException(response.errorBody()?.string()))
                    }
                } catch (e: Exception) {
                    onError(e)
                }
            }
        }
    }
}