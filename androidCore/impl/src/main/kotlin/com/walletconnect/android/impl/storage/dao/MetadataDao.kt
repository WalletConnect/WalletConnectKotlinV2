@file:JvmSynthetic

package com.walletconnect.android.impl.storage.dao

import android.database.sqlite.SQLiteException
import com.walletconnect.android.common.model.MetaDataType
import com.walletconnect.android.common.model.PeerMetaData
import com.walletconnect.android.common.model.Redirect
import com.walletconnect.android.impl.storage.data.dao.MetaDataQueries
import com.walletconnect.foundation.common.model.Topic

class MetadataDao(private val metaDataQueries: MetaDataQueries) {

    @Throws(SQLiteException::class)
    fun insertOrAbortMetadata(metaData: PeerMetaData, metaDataType: MetaDataType, topic: Topic) =
        with(metaData) { metaDataQueries.insertOrAbortMetaData(topic.value, name, description, url, icons, redirect?.native, metaDataType) }

    @Throws(SQLiteException::class)
    fun updateOrAbortMetaData(metaData: PeerMetaData, metaDataType: MetaDataType, topic: Topic) =
        with(metaData) { metaDataQueries.updateOrAbortMetaData(name, description, url, icons, redirect?.native, metaDataType, topic.value) }

    fun deleteMetaData(topic: Topic): Unit = metaDataQueries.deleteMetaDataFromTopic(topic.value)

    fun existsByTopic(topic: Topic): Boolean = metaDataQueries.getIdByTopic(topic.value).executeAsOneOrNull() == null

    fun getByTopic(topic: Topic): PeerMetaData = metaDataQueries.getMetadataByTopic(topic.value, mapper = this::toMetadata).executeAsOne()

    private fun toMetadata(peerName: String, peerDesc: String, peerUrl: String, peerIcons: List<String>, native: String?): PeerMetaData =
        PeerMetaData(peerName, peerDesc, peerUrl, peerIcons, Redirect(native = native))
}