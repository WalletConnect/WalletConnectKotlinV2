package com.walletconnect.android.common.storage

import com.walletconnect.android.common.model.MetaDataType
import com.walletconnect.android.common.model.PeerMetaData
import com.walletconnect.foundation.common.model.Topic

interface MetadataStorageRepositoryInterface {

    fun insertOrAbortMetadata(topic: Topic, metaData: PeerMetaData, metaDataType: MetaDataType)

    fun updateOrAbortMetaData(topic: Topic, metaData: PeerMetaData, metaDataType: MetaDataType)

    fun deleteMetaData(topic: Topic)

    fun existsByTopic(topic: Topic): Boolean

    fun getByTopic(topic: Topic): PeerMetaData

    fun upsertPairingPeerMetadata(topic: Topic, metaData: PeerMetaData, metaDataType: MetaDataType)
}