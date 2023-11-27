@file:OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)

package com.walletconnect.sample.wallet.domain

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.annotation.DrawableRes
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.toBitmap
import coil.ImageLoader
import coil.request.ImageRequest
import com.walletconnect.android.Core
import com.walletconnect.notify.client.Notify
import com.walletconnect.notify.client.NotifyClient
import com.walletconnect.sample.wallet.R
import com.walletconnect.sample.wallet.ui.Web3WalletActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.net.URI

object NotificationHandler {

    private const val W3W_CHANNEL_ID = "Web3Wallet"
    private val _notificationsFlow: MutableSharedFlow<Notification> = MutableSharedFlow()

    private sealed interface Notification {
        val messageId: Int
        val channelId: String
        val title: String
        val body: String

        data class Simple(override val messageId: Int, override val channelId: String, override val title: String, override val body: String) : Notification

        data class Decrypted(
            override val messageId: Int, override val channelId: String, override val title: String, override val body: String, val topic: String, val url: String?, val iconUrl: String?
        ) : Notification //Notify

        data class SessionProposal(
            override val messageId: Int,
            override val channelId: String,
            override val title: String,
            override val body: String,
            val name: String,
            val description: String,
            val url: String,
            val iconUrl: String?,
            val redirect: String
        ) : Notification

        data class SessionRequest(
            override val messageId: Int,
            override val channelId: String,
            override val title: String,
            override val body: String,
            val chainId: String?,
            val topic: String,
            val url: String?,
            val iconUrl: String?
        ) : Notification

        data class AuthRequest(
            override val messageId: Int,
            override val channelId: String,
            override val title: String,
            override val body: String,
            val topic: String,
            val url: String?,
            val iconUrl: String?
        ) : Notification
    }

    private data class NotificationsWithMetadata(val notifications: List<Notification>, val channelName: String, val iconUrl: String?)

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

    private fun Flow<Map<String, List<Notification>>>.addChannelName(): Flow<Map<String, NotificationsWithMetadata>> = this.map { notificationsMap ->
        notificationsMap.mapValues { (channelId, notifications) ->
            val (channelName: String, iconUrl: String?) = if (channelId == W3W_CHANNEL_ID) channelId to null else runCatching {
                val topic = (notifications.first() as Notification.Decrypted).topic

                // TODO discus with the team how to make it more dev friendly
                val appMetadata = NotifyClient.getActiveSubscriptions()[topic]?.metadata
                    ?: throw IllegalStateException("No active subscription for topic: $topic")

                val appDomain = URI(appMetadata.url).host
                    ?: throw IllegalStateException("Unable to parse domain from $appMetadata.url")

                val typeName = NotifyClient.getNotificationTypes(Notify.Params.NotificationTypes(appDomain))[channelId]?.name
                    ?: throw IllegalStateException("No notification type for topic:${topic} and type: $channelId")

                (appMetadata.name + ": " + typeName) to appMetadata.icons.firstOrNull()
            }.getOrElse {
                Timber.e(it)
                channelId to null
            }
            NotificationsWithMetadata(notifications, channelName, iconUrl)
        }
    }

    private fun Flow<Map<String, NotificationsWithMetadata>>.buildAndShowNotification(context: Context, notificationIntervalMillis: Long): Flow<Map<String, NotificationsWithMetadata>> = this
        .onEach { notificationsMap ->
            val notificationManager by lazy { context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }
            val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            for ((channelId, notificationsWithChannelName) in notificationsMap) {
                val (notifications, channelName, largeIconUrl) = notificationsWithChannelName

                val (messageId, title, body) = if (notifications.size > 1) {
                    Triple(notifications.hashCode(), "You have ${notifications.size} $channelName notifications!", notifications.reversed().joinToString(separator = "\n") { it.title })
                } else {
                    val notification = notifications.first()
                    Triple(notification.messageId, notification.title, notification.body)
                }

                val url = (notifications.first() as? Notification.Decrypted)?.url
                val pendingIntent = buildPendingIntent(context, url)

                showNotification(
                    context = context,
                    notificationManager = notificationManager,
                    pendingIntent = pendingIntent,
                    notificationId = messageId,
                    channelId = channelId,
                    channelName = channelName,
                    title = title,
                    body = body,
                    importance = NotificationManager.IMPORTANCE_HIGH,
                    defaultSoundUri = defaultSoundUri,
                    smallIcon = R.drawable.ic_walletconnect_logo,
                    largeIconUrl = largeIconUrl,
                    autoCancel = true,
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
        importance: Int, defaultSoundUri: Uri, @DrawableRes smallIcon: Int, largeIconUrl: String?, autoCancel: Boolean,
    ) {
        suspend fun fetchBitmapFromUrl(): Bitmap? {
            if (largeIconUrl == null) return null
            val imageLoader = ImageLoader(context)
            val request = ImageRequest.Builder(context)
                .data(largeIconUrl)
                .build()

            val drawable = imageLoader.execute(request).drawable
            return drawable?.toBitmap()
        }

        fun createNotificationAndNotify(bitmap: Bitmap?) {

            val notificationBuilder = NotificationCompat.Builder(context, channelId)
                .setContentTitle(title)
                .setSmallIcon(smallIcon)
                .setContentText(body)
                .setAutoCancel(autoCancel)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)

            if (bitmap != null) notificationBuilder.setLargeIcon(bitmap)

            // Since android Oreo notification channel is needed.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(channelId, channelName, importance)
                notificationManager.createNotificationChannel(channel)
            }

            notificationManager.notify(notificationId, notificationBuilder.build())
        }

        CoroutineScope(Dispatchers.IO).launch {
            val bitmap = fetchBitmapFromUrl()
            withContext(Dispatchers.Main) {
                createNotificationAndNotify(bitmap)
            }
        }
    }

    suspend fun addNotification(message: Core.Model.Message) {
        val notification = when (message) {
            is Core.Model.Message.Simple -> Notification.Simple(message.hashCode(), W3W_CHANNEL_ID, message.title, message.body)
            is Core.Model.Message.Notify -> Notification.Decrypted(message.hashCode(), message.type, message.title, message.body, message.topic, message.url, message.url)
            is Core.Model.Message.AuthRequest -> Notification.AuthRequest(
                message.hashCode(),
                W3W_CHANNEL_ID,
                "New Authentication Request!",
                "A new authentication request arrived from ${message.metadata.name}, please check your wallet",
                message.pairingTopic,
                message.metadata.url,
                message.metadata.icons.firstOrNull()
            )

            is Core.Model.Message.SessionRequest -> Notification.SessionRequest(
                message.hashCode(),
                W3W_CHANNEL_ID,
                "New session request!",
                "A new session request ${message.request.method} arrived from ${message.peerMetaData?.name}, please check your wallet",
                message.chainId,
                message.topic,
                message.peerMetaData?.url,
                message.peerMetaData?.icons?.firstOrNull()
            )

            is Core.Model.Message.SessionProposal -> Notification.SessionProposal(
                message.hashCode(),
                W3W_CHANNEL_ID,
                "New session proposal!",
                "A new session proposal arrived from ${message.name}, please check your wallet",
                message.name,
                message.description,
                message.url,
                message.icons.firstOrNull(),
                message.redirect
            )
        }

        _notificationsFlow.emit(notification)
    }


    suspend fun addNotification(message: Notify.Model.Message) {
        val notification =
            if (message is Notify.Model.Message.Decrypted) Notification.Decrypted(message.hashCode(), message.type, message.title, message.body, message.topic, message.url, message.url)
            else Notification.Simple(message.hashCode(), W3W_CHANNEL_ID, message.title, message.body)

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