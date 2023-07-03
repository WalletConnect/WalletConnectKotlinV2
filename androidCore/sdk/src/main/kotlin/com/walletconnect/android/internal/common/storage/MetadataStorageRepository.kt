package com.walletconnect.android.internal.common.storage

import android.database.sqlite.SQLiteException
import com.walletconnect.android.internal.common.model.AppMetaData
import com.walletconnect.android.internal.common.model.AppMetaDataType
import com.walletconnect.android.internal.common.model.Redirect
import com.walletconnect.android.sdk.storage.data.dao.MetaDataQueries
import com.walletconnect.foundation.common.model.Topic

class MetadataStorageRepository(private val metaDataQueries: MetaDataQueries): MetadataStorageRepositoryInterface {

    @Throws(SQLiteException::class)
    override fun insertOrAbortMetadata(topic: Topic, appMetaData: AppMetaData, appMetaDataType: AppMetaDataType) = with(appMetaData) {
        metaDataQueries.insertOrAbortMetaData(topic.value, name, description, url, icons, redirect?.native, appMetaDataType)
    }

    @Throws(SQLiteException::class)
    override fun updateMetaData(topic: Topic, appMetaData: AppMetaData, appMetaDataType: AppMetaDataType) = with(appMetaData) {
        metaDataQueries.updateMetaData(name, description, url, icons, redirect?.native, appMetaDataType, topic.value)
    }

    @Throws(SQLiteException::class)
    override fun upsertPairingPeerMetadata(topic: Topic, appMetaData: AppMetaData, appMetaDataType: AppMetaDataType) {
        if (!existsByTopicAndType(topic, appMetaDataType)) {
            insertOrAbortMetadata(topic, appMetaData, appMetaDataType)
        } else {
            updateMetaData(topic, appMetaData, appMetaDataType)
        }
    }

    override fun deleteMetaData(topic: Topic): Unit = metaDataQueries.deleteMetaDataFromTopic(topic.value)

    override fun existsByTopicAndType(topic: Topic, type: AppMetaDataType): Boolean = metaDataQueries.getIdByTopicAndType(topic.value, type).executeAsOneOrNull() != null

    override fun getByTopicAndType(topic: Topic, type: AppMetaDataType): AppMetaData? =
        metaDataQueries.getMetadataByTopicAndType(sequence_topic = topic.value, type = type, mapper = this::toMetadata).executeAsOneOrNull()

    override fun lastInsertedId(): Long = metaDataQueries.lastInsertedRowId().executeAsOne()

    override fun getIdByTopicAndType(topic: Topic, type: AppMetaDataType): Long = metaDataQueries.getIdByTopicAndType(topic.value, type).executeAsOne()

    private fun toMetadata(peerName: String, peerDesc: String, peerUrl: String, peerIcons: List<String>, native: String?): AppMetaData =
        AppMetaData(name = peerName, description = peerDesc, url = peerUrl, icons = peerIcons, redirect = Redirect(native = native))
}