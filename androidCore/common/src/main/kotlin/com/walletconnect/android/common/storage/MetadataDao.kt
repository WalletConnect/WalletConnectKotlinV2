@file:JvmSynthetic

package com.walletconnect.android.common.storage

import android.database.sqlite.SQLiteException
import com.walletconnect.android.common.model.metadata.PeerMetaData
import com.walletconnect.android.common.model.MetaDataType
import com.walletconnect.foundation.common.model.Topic
import comwalletconnectandroidcommonstoragedata.MetaDataQueries

class MetadataDao(private val metaDataQueries: MetaDataQueries) {

    @Throws(SQLiteException::class)
    fun insertOrAbortMetadata(metaData: PeerMetaData, metaDataType: MetaDataType, topic: Topic) =
        with(metaData) { metaDataQueries.insertOrAbortMetaData(topic.value, name, description, url, icons, redirect?.native, metaDataType) }

    @Throws(SQLiteException::class)
    fun updateOrAbortMetaData(metaData: PeerMetaData, metaDataType: MetaDataType, topic: Topic) =
        with(metaData) { metaDataQueries.updateOrAbortMetaData(name, description, url, icons, redirect?.native, metaDataType, topic.value) }

    @JvmSynthetic
    fun deleteMetaData(topic: Topic): Unit = metaDataQueries.deleteMetaDataFromTopic(topic.value)

    @JvmSynthetic
    fun existsByTopic(topic: Topic): Boolean = metaDataQueries.getIdByTopic(topic.value).executeAsOneOrNull() == null
}