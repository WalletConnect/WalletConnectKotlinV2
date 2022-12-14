@file:JvmSynthetic

package com.walletconnect.chat.di

import org.koin.dsl.module
import com.walletconnect.android.internal.common.di.commonModule as androidCommonModule

@JvmSynthetic
internal fun commonModule() = module {
    includes(androidCommonModule())
}