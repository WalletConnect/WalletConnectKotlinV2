package com.walletconnect.android.common.storage

import com.walletconnect.android.common.model.MetaDataType
import com.walletconnect.android.common.model.MetaData
import com.walletconnect.foundation.common.model.Topic

interface MetadataStorageRepositoryInterface {

    fun insertOrAbortMetadata(topic: Topic, metaData: MetaData, metaDataType: MetaDataType)

    fun updateOrAbortMetaData(topic: Topic, metaData: MetaData, metaDataType: MetaDataType)

    fun deleteMetaData(topic: Topic)

    fun existsByTopic(topic: Topic): Boolean

    fun getByTopic(topic: Topic): MetaData

    fun upsertPairingPeerMetadata(topic: Topic, metaData: MetaData, metaDataType: MetaDataType)
}