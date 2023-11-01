@file:OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)

package com.walletconnect.sample.wallet.domain

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.annotation.DrawableRes
import androidx.core.app.NotificationCompat
import com.walletconnect.notify.client.Notify
import com.walletconnect.notify.client.NotifyClient
import com.walletconnect.sample.wallet.ui.Web3WalletActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import java.net.URI

object NotificationHandler {

    private const val SIMPLE_CHANNEL_ID = "Web3Wallet"
    private val _notificationsFlow: MutableSharedFlow<Notification> = MutableSharedFlow()

    private sealed interface Notification {
        val messageId: Int
        val channelId: String
        val title: String
        val body: String

        data class Simple(override val messageId: Int, override val channelId: String, override val title: String, override val body: String) : Notification

        data class Decrypted(override val messageId: Int, override val channelId: String, override val title: String, override val body: String, val topic: String, val url: String?) : Notification
    }

    private data class NotificationsWithChannelName(val notifications: List<Notification>, val channelName: String)

    private fun <T, R> Flow<T>.bufferWithDebounce(debounceMillis: Long, transform: (List<T>) -> R): Flow<R> = channelFlow {
        val buffer = mutableListOf<T>()

        this@bufferWithDebounce
            .onEach { value -> buffer.add(value) }
            .debounce(debounceMillis)
            .collect {
                val collected = buffer.toList()
                send(transform(collected))
                buffer.removeAll(collected)
            }
    }

    private fun Flow<Notification>.debounceUniqueAndGroupByChannelId(debounceMillis: Long): Flow<Map<String, List<Notification>>> = this.buffer()
        .bufferWithDebounce(debounceMillis) { notifications -> notifications.distinctBy { it.messageId }.groupBy { it.channelId } }

    private fun Flow<Map<String, List<Notification>>>.addChannelName(): Flow<Map<String, NotificationsWithChannelName>> = this.map { notificationsMap ->
        notificationsMap.mapValues { (channelId, notifications) ->
            val channelName = if (channelId == SIMPLE_CHANNEL_ID) channelId else runCatching {
                val topic = (notifications.first() as Notification.Decrypted).topic

                // TODO discus with the team how to make it more dev friendly
                val appMetadata = NotifyClient.getActiveSubscriptions()[topic]?.metadata
                    ?: throw IllegalStateException("No active subscription for topic: $topic")

                val appDomain = URI(appMetadata.url).host
                    ?: throw IllegalStateException("Unable to parse domain from $appMetadata.url")

                val typeName = NotifyClient.getNotificationTypes(Notify.Params.NotificationTypes(appDomain))[channelId]?.name
                    ?: throw IllegalStateException("No notification type for topic:${topic} and type: $channelId")

                (appMetadata.name + ": " + typeName)
            }.getOrElse {
                Timber.e(it)
                channelId
            }
            NotificationsWithChannelName(notifications, channelName)
        }
    }

    private fun Flow<Map<String, NotificationsWithChannelName>>.buildAndShowNotification(context: Context, notificationIntervalMillis: Long): Flow<Map<String, NotificationsWithChannelName>> = this
        .onEach { notificationsMap ->
            val notificationManager by lazy { context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }
            val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            for ((channelId, notificationsWithChannelName) in notificationsMap) {
                val (notifications, channelName) = notificationsWithChannelName

                val (messageId, title, body) = if (notifications.size > 1) {
                    Triple(notifications.hashCode(), "You have ${notifications.size} $channelName notifications!", notifications.reversed().joinToString(separator = "\n") { it.title })
                } else {
                    val notification = notifications.first()
                    Triple(notification.messageId, notification.title, notification.body)
                }

                val url = (notifications.first() as? Notification.Decrypted)?.url
                val pendingIntent = buildPendingIntent(context, url)

                showNotification(
                    context = context, notificationManager = notificationManager, pendingIntent = pendingIntent, notificationId = messageId, channelId = channelId, channelName = channelName,
                    title = title, body = body, importance = NotificationManager.IMPORTANCE_HIGH, defaultSoundUri = defaultSoundUri, icon = android.R.drawable.ic_popup_reminder, autoCancel = true,
                )

                delay(notificationIntervalMillis)
            }
        }

    private fun buildPendingIntent(context: Context, url: String?): PendingIntent {
        val parsedUrl = kotlin.runCatching { Uri.parse(url) }.getOrNull()
        val intent = if (parsedUrl == null) {
            Intent(context, Web3WalletActivity::class.java).apply { addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP) }
        } else {
            Intent(Intent.ACTION_VIEW, parsedUrl)
        }
        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
    }

    private fun showNotification(
        context: Context, notificationManager: NotificationManager, pendingIntent: PendingIntent, notificationId: Int, channelId: String, channelName: String, title: String, body: String,
        importance: Int, defaultSoundUri: Uri, @DrawableRes icon: Int, autoCancel: Boolean,
    ) {

        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setContentTitle(title)
            .setSmallIcon(icon)
            .setContentText(body)
            .setAutoCancel(autoCancel)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName, importance)
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(notificationId, notificationBuilder.build())
    }

    suspend fun addNotification(message: Notify.Model.Message) {
        val notification =
            if (message is Notify.Model.Message.Decrypted) Notification.Decrypted(message.hashCode(), message.type, message.title, message.body, message.topic, message.url)
            else Notification.Simple(message.hashCode(), SIMPLE_CHANNEL_ID, message.title, message.body)

        _notificationsFlow.emit(notification)
    }

    @SuppressLint("InlinedApi")
    fun startNotificationDisplayingJob(coroutineScope: CoroutineScope, context: Context) {
        _notificationsFlow
            .debounceUniqueAndGroupByChannelId(2000)
            .addChannelName()
            .buildAndShowNotification(context, 5000)
            .launchIn(coroutineScope)
    }
}