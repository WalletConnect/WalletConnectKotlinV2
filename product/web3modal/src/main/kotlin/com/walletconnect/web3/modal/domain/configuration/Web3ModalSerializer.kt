package com.walletconnect.web3.modal.domain.configuration

import com.squareup.moshi.Moshi
import com.walletconnect.android.internal.common.wcKoinApp
import com.walletconnect.web3.modal.di.WEB3MODAL_MOSHI
import org.koin.core.qualifier.named

internal object Web3ModalConfigSerializer {
    val moshi: Moshi
        get() = wcKoinApp.koin.get(named(WEB3MODAL_MOSHI))

    fun deserialize(json: String): Config? {
        return tryDeserialize<Config>(json)
    }

    fun serialize(config: Config): String {
        return trySerialize(config)
    }

    private inline fun <reified T> tryDeserialize(json: String): T? = runCatching { moshi.adapter(T::class.java).fromJson(json) }.getOrNull()
    private inline fun <reified T> trySerialize(type: T): String = moshi.adapter(T::class.java).toJson(type)
}