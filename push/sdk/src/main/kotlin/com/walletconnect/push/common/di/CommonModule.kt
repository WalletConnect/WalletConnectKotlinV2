@file:JvmSynthetic

package com.walletconnect.push.common.di

import com.walletconnect.utils.addSdkBitsetForUA
import org.koin.dsl.module
import java.util.*

private const val BIT_ORDER = 3 // https://github.com/WalletConnect/walletconnect-docs/blob/main/docs/specs/clients/core/relay/relay-user-agent.md#schema
private val bitset: BitSet
    get() = BitSet(BIT_ORDER).apply {
        set(BIT_ORDER)
    }

@JvmSynthetic
internal fun commonModule() = module {

    addSdkBitsetForUA(bitset)
}