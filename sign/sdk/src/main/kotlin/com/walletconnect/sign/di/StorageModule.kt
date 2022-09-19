@file:JvmSynthetic

package com.walletconnect.sign.di

import com.squareup.sqldelight.ColumnAdapter
import com.squareup.sqldelight.EnumColumnAdapter
import com.walletconnect.android.impl.di.coreStorageModule
import com.walletconnect.sign.Database
import com.walletconnect.android.impl.common.model.type.enums.MetaDataType
import com.walletconnect.sign.storage.data.dao.metadata.MetaDataDao
import com.walletconnect.sign.storage.data.dao.namespace.NamespaceDao
import com.walletconnect.sign.storage.data.dao.namespace.NamespaceExtensionsDao
import com.walletconnect.sign.storage.data.dao.proposalnamespace.ProposalNamespaceDao
import com.walletconnect.sign.storage.data.dao.proposalnamespace.ProposalNamespaceExtensionsDao
import com.walletconnect.sign.storage.data.dao.temp.TempNamespaceDao
import com.walletconnect.sign.storage.data.dao.temp.TempNamespaceExtensionsDao
import com.walletconnect.sign.storage.sequence.SequenceStorageRepository
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

@JvmSynthetic
internal fun storageModule(storageSuffix: String): Module = module {

    includes(coreStorageModule<Database>(Database.Schema, storageSuffix))

    single<ColumnAdapter<MetaDataType, String>>(named(SignDITags.METADATA_TYPE)) { EnumColumnAdapter() }

    single {
        Database(
            get(),
            MetaDataDaoAdapter = MetaDataDao.Adapter(
                iconsAdapter = get(),
                typeAdapter = get(named(SignDITags.METADATA_TYPE))
            ),
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
        get<Database>().pairingDaoQueries
    }

    single {
        get<Database>().sessionDaoQueries
    }

    single {
        get<Database>().metaDataDaoQueries
    }

    single {
        get<Database>().namespaceDaoQueries
    }

    single {
        get<Database>().namespaceExtensionDaoQueries
    }

    single {
        get<Database>().tempNamespaceDaoQueries
    }

    single {
        get<Database>().tempNamespaceExtensionDaoQueries
    }

    single {
        get<Database>().proposalNamespaceDaoQueries
    }

    single {
        get<Database>().proposalNamespaceExtensionDaoQueries
    }

    single {
        SequenceStorageRepository(get(), get(), get(), get(), get(), get(), get(), get(), get())
    }
}
