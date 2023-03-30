@file:JvmSynthetic

package com.walletconnect.sign.di

import com.squareup.moshi.Moshi
import com.tinder.scarlet.utils.getRawType
import com.walletconnect.android.internal.common.di.AndroidCommonDITags
import com.walletconnect.sign.common.adapters.SessionRequestVOJsonAdapter
import com.walletconnect.sign.common.model.vo.clientsync.session.payload.SessionRequestVO
import com.walletconnect.utils.addSdkBitset
import org.koin.core.qualifier.named
import org.koin.dsl.module
import java.util.*
import kotlin.reflect.jvm.jvmName

private const val BIT_ORDER = 0 // https://github.com/WalletConnect/walletconnect-docs/blob/main/docs/specs/clients/core/relay/relay-user-agent.md#schema
private val bitset: BitSet
    get() = BitSet(BIT_ORDER).apply {
        set(BIT_ORDER, true)
    }

@JvmSynthetic
internal fun commonModule() = module {

    addSdkBitset("SignSDK", bitset)

    single {
        get<Moshi.Builder>(named(AndroidCommonDITags.MOSHI))
            .add { type, _, moshi ->
                when (type.getRawType().name) {
                    SessionRequestVO::class.jvmName -> SessionRequestVOJsonAdapter(moshi)
                    else -> null
                }
            }
    }
}