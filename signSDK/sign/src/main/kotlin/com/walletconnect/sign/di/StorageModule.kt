package com.walletconnect.sign.di

import com.squareup.sqldelight.ColumnAdapter
import com.squareup.sqldelight.EnumColumnAdapter
import com.walletconnect.android_core.di.storageModule
import com.walletconnect.sign.Database
import com.walletconnect.sign.core.model.type.enums.MetaDataType
import com.walletconnect.sign.storage.data.dao.metadata.MetaDataDao
import com.walletconnect.sign.storage.data.dao.namespace.NamespaceDao
import com.walletconnect.sign.storage.data.dao.namespace.NamespaceExtensionsDao
import com.walletconnect.sign.storage.data.dao.proposalnamespace.ProposalNamespaceDao
import com.walletconnect.sign.storage.data.dao.proposalnamespace.ProposalNamespaceExtensionsDao
import com.walletconnect.sign.storage.data.dao.temp.TempNamespaceDao
import com.walletconnect.sign.storage.data.dao.temp.TempNamespaceExtensionsDao
import com.walletconnect.sign.storage.sequence.SequenceStorageRepository
import com.walletconnect.sign.util.Empty
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

@JvmSynthetic
internal fun storageModule(): Module = module {

    includes(storageModule(String.Empty)) //due to db name and sharedPrefs key should stay the same for sign

    single<ColumnAdapter<List<String>, String>> {
        object : ColumnAdapter<List<String>, String> {

            override fun decode(databaseValue: String) =
                if (databaseValue.isBlank()) {
                    listOf()
                } else {
                    databaseValue.split(",")
                }

            override fun encode(value: List<String>) = value.joinToString(separator = ",")
        }
    }

    single<ColumnAdapter<MetaDataType, String>>(named("MetaDataType")) {
        EnumColumnAdapter()
    }

    single {
        Database(
            get(),
            MetaDataDaoAdapter = MetaDataDao.Adapter(
                iconsAdapter = get(),
                typeAdapter = get(named("MetaDataType"))
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
