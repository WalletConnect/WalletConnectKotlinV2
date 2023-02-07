@file:JvmSynthetic

package com.walletconnect.chat.di

import com.walletconnect.android.internal.common.di.AndroidCommonDITags
import com.walletconnect.chat.authentication.jwt.InviteKeyJwtRepository
import com.walletconnect.chat.engine.domain.ChatEngine
import org.koin.core.qualifier.named
import org.koin.dsl.module

@JvmSynthetic
internal fun engineModule() = module {

    single { InviteKeyJwtRepository() }

    single { ChatEngine(get(named(ChatDITags.KEYSERVER_URL)), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(named(AndroidCommonDITags.LOGGER))) }
}