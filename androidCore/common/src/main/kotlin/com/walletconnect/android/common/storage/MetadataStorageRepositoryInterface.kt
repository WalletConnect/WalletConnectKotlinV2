package com.walletconnect.android.common.storage

import com.walletconnect.android.common.model.MetaDataType
import com.walletconnect.android.common.model.PeerMetaData
import com.walletconnect.foundation.common.model.Topic

interface MetadataStorageRepositoryInterface {

    fun insertOrAbortMetadata(metaData: PeerMetaData, metaDataType: MetaDataType, topic: Topic)

    fun updateOrAbortMetaData(metaData: PeerMetaData, metaDataType: MetaDataType, topic: Topic)

    fun deleteMetaData(topic: Topic)

    fun existsByTopic(topic: Topic): Boolean

    fun getByTopic(topic: Topic): PeerMetaData
}