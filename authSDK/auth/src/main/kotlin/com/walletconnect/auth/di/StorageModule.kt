package com.walletconnect.auth.di


import com.squareup.sqldelight.ColumnAdapter
import com.squareup.sqldelight.EnumColumnAdapter
import com.walletconnect.android_core.common.model.type.enums.MetaDataType
import com.walletconnect.android_core.di.coreStorageModule
import com.walletconnect.auth.Database
import com.walletconnect.auth.storage.AuthStorageRepository
import com.walletconnect.auth.storage.data.dao.MetaDataDao
import org.koin.dsl.module

@JvmSynthetic
internal fun storageModule(storageSuffix: String) = module {

    includes(coreStorageModule<Database>(Database.Schema, storageSuffix))

    single<ColumnAdapter<MetaDataType, String>> { EnumColumnAdapter() }

    single {
        Database(
            get(),
            MetaDataDaoAdapter = MetaDataDao.Adapter(
                iconsAdapter = get(),
                typeAdapter = get()
            )
        )
    }

    single {
        get<Database>().pairingDaoQueries
    }

    single {
        get<Database>().metaDataDaoQueries
    }

    single {
        AuthStorageRepository(get(), get())
    }
}