@file:JvmSynthetic

package com.walletconnect.sign.di

import com.walletconnect.android.impl.di.coreStorageModule
import com.walletconnect.android.impl.di.sdkBaseStorageModule
import com.walletconnect.sign.SignDatabase
import com.walletconnect.sign.storage.data.dao.namespace.NamespaceDao
import com.walletconnect.sign.storage.data.dao.namespace.NamespaceExtensionsDao
import com.walletconnect.sign.storage.data.dao.proposalnamespace.ProposalNamespaceDao
import com.walletconnect.sign.storage.data.dao.proposalnamespace.ProposalNamespaceExtensionsDao
import com.walletconnect.sign.storage.data.dao.temp.TempNamespaceDao
import com.walletconnect.sign.storage.data.dao.temp.TempNamespaceExtensionsDao
import com.walletconnect.sign.storage.sequence.SessionStorageRepository
import org.koin.core.module.Module
import org.koin.dsl.module

@JvmSynthetic
internal fun storageModule(storageSuffix: String): Module = module {

    includes(coreStorageModule(), sdkBaseStorageModule(SignDatabase.Schema, storageSuffix))

    single {
        SignDatabase(
            get(),
            NamespaceDaoAdapter = NamespaceDao.Adapter(
                accountsAdapter = get(),
                methodsAdapter = get(),
                eventsAdapter = get()
            ),
            NamespaceExtensionsDaoAdapter = NamespaceExtensionsDao.Adapter(
                accountsAdapter = get(),
                methodsAdapter = get(),
                eventsAdapter = get()
            ),
            TempNamespaceDaoAdapter = TempNamespaceDao.Adapter(
                accountsAdapter = get(),
                methodsAdapter = get(),
                eventsAdapter = get()
            ),
            TempNamespaceExtensionsDaoAdapter = TempNamespaceExtensionsDao.Adapter(
                accountsAdapter = get(),
                methodsAdapter = get(),
                eventsAdapter = get()
            ),
            ProposalNamespaceDaoAdapter = ProposalNamespaceDao.Adapter(
                chainsAdapter = get(),
                methodsAdapter = get(),
                eventsAdapter = get()
            ),
            ProposalNamespaceExtensionsDaoAdapter = ProposalNamespaceExtensionsDao.Adapter(
                chainsAdapter = get(),
                methodsAdapter = get(),
                eventsAdapter = get()
            )
        )
    }

    single {
        get<SignDatabase>().sessionDaoQueries
    }

    single {
        get<SignDatabase>().namespaceDaoQueries
    }

    single {
        get<SignDatabase>().namespaceExtensionDaoQueries
    }

    single {
        get<SignDatabase>().tempNamespaceDaoQueries
    }

    single {
        get<SignDatabase>().tempNamespaceExtensionDaoQueries
    }

    single {
        get<SignDatabase>().proposalNamespaceDaoQueries
    }

    single {
        get<SignDatabase>().proposalNamespaceExtensionDaoQueries
    }

    single {
        SessionStorageRepository(get(), get(), get(), get(), get(), get(), get())
    }
}
