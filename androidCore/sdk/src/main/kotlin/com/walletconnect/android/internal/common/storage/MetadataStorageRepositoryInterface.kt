package com.walletconnect.android.internal.common.storage

import com.walletconnect.android.internal.common.model.AppMetaData
import com.walletconnect.android.internal.common.model.AppMetaDataType
import com.walletconnect.foundation.common.model.Topic

interface MetadataStorageRepositoryInterface {

    fun insertOrAbortMetadata(topic: Topic, appMetaData: AppMetaData, appMetaDataType: AppMetaDataType)

    fun updateMetaData(topic: Topic, appMetaData: AppMetaData, appMetaDataType: AppMetaDataType)

    fun deleteMetaData(topic: Topic)

    fun existsByTopicAndType(topic: Topic, type: AppMetaDataType): Boolean

    fun getByTopicAndType(topic: Topic, type: AppMetaDataType): AppMetaData?

    fun upsertPairingPeerMetadata(topic: Topic, appMetaData: AppMetaData, appMetaDataType: AppMetaDataType)
}