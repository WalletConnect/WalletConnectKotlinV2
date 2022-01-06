package com.walletconnect.walletconnectv2.di

import com.walletconnect.walletconnectv2.common.AppMetaData
import com.walletconnect.walletconnectv2.engine.EngineInteractor
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory

@AssistedFactory
internal interface EngineFactory {

    fun create(
        @Assisted isController: Boolean,
        @Assisted metaData: AppMetaData
    ): EngineInteractor
}