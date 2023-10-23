package com.walletconnect.web3.modal.client

import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.internal.common.wcKoinApp
import com.walletconnect.web3.modal.domain.usecase.DeleteSessionDataUseCase
import com.walletconnect.web3.modal.domain.usecase.GetSelectedChainUseCase
import com.walletconnect.web3.modal.domain.usecase.GetSessionTopicUseCase
import kotlinx.coroutines.launch

internal object Session {

    private val getSessionTopicUseCase: GetSessionTopicUseCase = wcKoinApp.koin.get()
    private val getSelectedChainUseCase: GetSelectedChainUseCase = wcKoinApp.koin.get()
    private val deleteSessionDataUseCase: DeleteSessionDataUseCase = wcKoinApp.koin.get()

    fun getSessionTopic(): String? = getSessionTopicUseCase()

    fun getSelectedChainId(): String? = getSelectedChainUseCase()

    fun clearSessionData() {
        scope.launch { deleteSessionDataUseCase() }
    }
}