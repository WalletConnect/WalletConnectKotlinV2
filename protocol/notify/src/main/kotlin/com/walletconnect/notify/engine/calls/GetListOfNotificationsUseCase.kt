@file:JvmSynthetic

package com.walletconnect.notify.engine.calls

import com.walletconnect.android.internal.common.model.AppMetaDataType
import com.walletconnect.android.internal.common.storage.metadata.MetadataStorageRepositoryInterface
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.notify.common.model.NotifyMessage
import com.walletconnect.notify.common.model.NotifyRecord
import com.walletconnect.notify.data.storage.NotificationsRepository
import kotlinx.coroutines.supervisorScope

internal class GetListOfNotificationsUseCase(
    private val notificationsRepository: NotificationsRepository,
    private val metadataStorageRepository: MetadataStorageRepositoryInterface,
) : GetListOfNotificationsUseCaseInterface {

    override suspend fun getListOfNotifications(topic: String): Map<Long, NotifyRecord> = supervisorScope {
        val dappMetaData = metadataStorageRepository.getByTopicAndType(Topic(topic), AppMetaDataType.PEER)
            ?: throw IllegalStateException("Dapp metadata does not exists for $topic")

        notificationsRepository
            .getNotificationsByTopic(topic)
            .map { notifyRecord ->
                with(notifyRecord) {
                    NotifyRecord(
                        id = id, topic = topic, publishedAt = publishedAt, metadata = dappMetaData,
                        notifyMessage = NotifyMessage(title = notifyMessage.title, body = notifyMessage.body, icon = notifyMessage.icon, url = notifyMessage.url, type = notifyMessage.type),
                    )
                }
            }
            .associateBy { notifyRecord -> notifyRecord.id }
    }
}

internal interface GetListOfNotificationsUseCaseInterface {
    suspend fun getListOfNotifications(topic: String): Map<Long, NotifyRecord>
}