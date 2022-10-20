@file:JvmSynthetic

package com.walletconnect.sign.di

import com.walletconnect.android.impl.di.AndroidCoreDITags
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
import org.koin.core.qualifier.named
import org.koin.dsl.module

@JvmSynthetic
internal fun storageModule(storageSuffix: String): Module = module {

    includes(coreStorageModule(), sdkBaseStorageModule(SignDatabase.Schema, storageSuffix))

    single {
        SignDatabase(
            get(),
            NamespaceDaoAdapter = NamespaceDao.Adapter(
                accountsAdapter = get(named(AndroidCoreDITags.COLUMN_ADAPTER_LIST)),
                methodsAdapter = get(named(AndroidCoreDITags.COLUMN_ADAPTER_LIST)),
                eventsAdapter = get(named(AndroidCoreDITags.COLUMN_ADAPTER_LIST))
            ),
            NamespaceExtensionsDaoAdapter = NamespaceExtensionsDao.Adapter(
                accountsAdapter = get(named(AndroidCoreDITags.COLUMN_ADAPTER_LIST)),
                methodsAdapter = get(named(AndroidCoreDITags.COLUMN_ADAPTER_LIST)),
                eventsAdapter = get(named(AndroidCoreDITags.COLUMN_ADAPTER_LIST))
            ),
            TempNamespaceDaoAdapter = TempNamespaceDao.Adapter(
                accountsAdapter = get(named(AndroidCoreDITags.COLUMN_ADAPTER_LIST)),
                methodsAdapter = get(named(AndroidCoreDITags.COLUMN_ADAPTER_LIST)),
                eventsAdapter = get(named(AndroidCoreDITags.COLUMN_ADAPTER_LIST))
            ),
            TempNamespaceExtensionsDaoAdapter = TempNamespaceExtensionsDao.Adapter(
                accountsAdapter = get(named(AndroidCoreDITags.COLUMN_ADAPTER_LIST)),
                methodsAdapter = get(named(AndroidCoreDITags.COLUMN_ADAPTER_LIST)),
                eventsAdapter = get(named(AndroidCoreDITags.COLUMN_ADAPTER_LIST))
            ),
            ProposalNamespaceDaoAdapter = ProposalNamespaceDao.Adapter(
                chainsAdapter = get(named(AndroidCoreDITags.COLUMN_ADAPTER_LIST)),
                methodsAdapter = get(named(AndroidCoreDITags.COLUMN_ADAPTER_LIST)),
                eventsAdapter = get(named(AndroidCoreDITags.COLUMN_ADAPTER_LIST))
            ),
            ProposalNamespaceExtensionsDaoAdapter = ProposalNamespaceExtensionsDao.Adapter(
                chainsAdapter = get(named(AndroidCoreDITags.COLUMN_ADAPTER_LIST)),
                methodsAdapter = get(named(AndroidCoreDITags.COLUMN_ADAPTER_LIST)),
                eventsAdapter = get(named(AndroidCoreDITags.COLUMN_ADAPTER_LIST))
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
