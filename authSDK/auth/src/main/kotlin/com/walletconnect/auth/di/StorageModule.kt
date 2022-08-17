package com.walletconnect.auth.di


import com.squareup.sqldelight.ColumnAdapter
import com.squareup.sqldelight.EnumColumnAdapter
import com.walletconnect.android_core.common.model.type.enums.MetaDataType
import com.walletconnect.android_core.di.coreStorageModule
import com.walletconnect.auth.Database
import com.walletconnect.auth.storage.data.dao.MetaDataDao
import org.koin.core.qualifier.named
import org.koin.dsl.module

@JvmSynthetic
internal fun storageModule() = module {

    val dbSuffix = "_auth"
    includes(coreStorageModule<Database>(Database.Schema, dbSuffix))

    single<ColumnAdapter<MetaDataType, String>>(named(AuthDITags.METADATA_TYPE)) { EnumColumnAdapter() }

    single {
        Database(
            get(),
            MetaDataDaoAdapter = MetaDataDao.Adapter(
                iconsAdapter = get(),
                typeAdapter = get(named(AuthDITags.METADATA_TYPE))
            )
        )
    }

    single {
        get<Database>().pairingDaoQueries
    }
}