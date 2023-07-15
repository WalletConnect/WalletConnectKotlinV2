package com.walletconnect.sample.wallet.domain.model

import com.walletconnect.push.common.Push
import com.walletconnect.sample.common.convertSecondsToDate

data class PushNotification(
    val id: String,
    val topic: String,
    val date: String,
    val title: String,
    val body: String,
    val url: String?,
    val icon: String?
)

fun Push.Wallet.Event.Message.toPushNotification() = PushNotification(
    id = message.id,
    topic = message.topic,
    date = message.publishedAt.convertSecondsToDate(),
    title = message.message.title,
    body = message.message.body,
    url = message.message.url,
    icon = message.message.icon
)

fun Push.Model.MessageRecord.toPushNotification() = PushNotification(
    id = id,
    topic = topic,
    date = publishedAt.convertSecondsToDate(),
    title = message.title,
    body = message.body,
    url = message.url,
    icon = message.icon
)