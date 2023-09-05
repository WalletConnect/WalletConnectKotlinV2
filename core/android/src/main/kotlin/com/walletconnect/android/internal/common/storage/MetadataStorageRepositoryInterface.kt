package com.walletconnect.android.internal.common.storage

import com.walletconnect.android.internal.common.model.AppMetaData
import com.walletconnect.android.internal.common.model.AppMetaDataType
import com.walletconnect.foundation.common.model.Topic

interface MetadataStorageRepositoryInterface {

    fun insertOrAbortMetadata(topic: Topic, appMetaData: AppMetaData, appMetaDataType: AppMetaDataType)

    fun updateMetaData(topic: Topic, appMetaData: AppMetaData, appMetaDataType: AppMetaDataType)

    suspend fun updateOrAbortMetaDataTopic(oldTopic: Topic, newTopic: Topic)

    fun deleteMetaData(topic: Topic)

    fun existsByTopicAndType(topic: Topic, type: AppMetaDataType): Boolean

    fun getByTopicAndType(topic: Topic, type: AppMetaDataType): AppMetaData?

    fun upsertPeerMetadata(topic: Topic, appMetaData: AppMetaData, appMetaDataType: AppMetaDataType)
}