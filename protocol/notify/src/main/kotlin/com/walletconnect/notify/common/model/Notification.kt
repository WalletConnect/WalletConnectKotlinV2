@file:JvmSynthetic

package com.walletconnect.notify.common.model

import com.walletconnect.android.internal.common.model.AppMetaData
import com.walletconnect.android.internal.common.model.type.EngineEvent

internal data class Notification(
    val id: String,
    val topic: String,
    val sentAt: Long,
    val notificationMessage: NotificationMessage,
    val metadata: AppMetaData?,
    val isLast: Boolean = false,
) : EngineEvent