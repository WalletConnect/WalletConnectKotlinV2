package com.walletconnect.sync.engine.domain

import com.walletconnect.android.internal.common.model.type.EngineEvent
import com.walletconnect.sync.engine.use_case.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

internal class SyncEngine(
    private val getStoresUseCase: GetStoresUseCase,
    private val registerUseCase: RegisterUseCase,
    private val createUseCase: CreateUseCase,
    private val deleteUseCase: DeleteUseCase,
    private val setUseCase: SetUseCase,
) : GetMessageUseCaseInterface by GetMessageUseCase,
    CreateUseCaseInterface by createUseCase,
    GetStoresUseCaseInterface by getStoresUseCase,
    RegisterUseCaseInterface by registerUseCase,
    DeleteUseCaseInterface by deleteUseCase,
    SetUseCaseInterface by setUseCase {

    private val _events: MutableSharedFlow<EngineEvent> = MutableSharedFlow()
    val events: SharedFlow<EngineEvent> = _events.asSharedFlow()

    fun setup() {
        TODO()
    }

}