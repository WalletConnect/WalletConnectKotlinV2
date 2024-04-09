package com.walletconnect.web3.modal.di

import com.walletconnect.android.internal.common.di.AndroidCommonDITags
import com.walletconnect.android.internal.common.model.AppMetaData
import com.walletconnect.web3.modal.domain.magic.handler.MagicController
import com.walletconnect.web3.modal.ui.components.internal.email.ActivityLifeCycleWatcher
import com.walletconnect.web3.modal.ui.components.internal.email.webview.MagicWebViewManager
import org.koin.android.ext.koin.androidApplication
import org.koin.core.qualifier.named
import org.koin.dsl.module

internal fun magicModule() = module {

    single(named(AndroidCommonDITags.SECURE_SITE_HEADERS)) {
        mapOf(
            Pair("referer", get<AppMetaData>().url),
            Pair("x-bundle-id", get(named(AndroidCommonDITags.BUNDLE_ID))),
        )
    }

    single {
        MagicWebViewManager(
            bundleId = get(named(AndroidCommonDITags.BUNDLE_ID)),
            projectId = get(),
            appData = get(),
            logger = get(named(AndroidCommonDITags.LOGGER)),
            headers = get(named(AndroidCommonDITags.SECURE_SITE_HEADERS))
        )
    }

    single {
        ActivityLifeCycleWatcher(
            application = androidApplication(),
            magicWebViewManager = get()
        )
    }

    single {
        MagicController(magicWebViewManager = get())
    }
}