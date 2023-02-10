package com.walletconnect.chat.client

import androidx.annotation.Keep
import com.walletconnect.android.CoreClient
import com.walletconnect.android.cacao.SignatureInterface

object Chat {
    sealed interface Listeners {
        fun onError(error: Model.Error)

        interface PublicKeyOnSuccess : Listeners {
            fun onSuccess(publicKey: String)
        }

        interface SignMessage : Listeners {
            fun onSign(message: String): Model.Cacao.Signature
        }

        interface Resolve : PublicKeyOnSuccess
        interface Register : PublicKeyOnSuccess, SignMessage
        interface Unregister : PublicKeyOnSuccess, SignMessage
    }

    sealed interface Type {
        enum class InviteStatus : Type { PENDING, REJECTED, APPROVED }

        @JvmInline
        value class AccountId(val value: String) : Type

        @JvmInline
        value class InviteMessage(val value: String) : Type

        @JvmInline
        value class MediaData(val value: String) : Type

        @JvmInline
        value class ChatMessage(val value: String) : Type
    }

    sealed class Model {
        data class Error(val throwable: Throwable) : Model()

        data class ConnectionState(val isAvailable: Boolean) : Model()

        data class SentInvite(
            val id: Long,
            val inviterAccount: Type.AccountId,
            val inviteeAccount: Type.AccountId,
            val message: Type.InviteMessage,
            val status: Type.InviteStatus,
        ) : Model()

        data class ReceivedInvite(
            val id: Long,
            val inviterAccount: Type.AccountId,
            val inviteeAccount: Type.AccountId,
            val message: Type.InviteMessage,
            val inviterPublicKey: String,
            val inviteePublicKey: String,
        )

        data class Invite(
            val inviterAccount: Type.AccountId,
            val inviteeAccount: Type.AccountId,
            val message: Type.InviteMessage,
            val inviteePublicKey: String,
        ) : Model()

        data class Media(
            val type: String,
            val data: Type.MediaData,
        ) : Model()

        data class Thread(
            val topic: String,
            val selfAccount: Type.AccountId,
            val peerAccount: Type.AccountId,
        ) : Model()

        data class Message(
            val topic: String,
            val message: Type.ChatMessage,
            val authorAccount: Type.AccountId,
            val timestamp: Long,
            val media: Media?,
        ) : Model()

        sealed class Events : Model() {
            data class OnInvite(val invite: ReceivedInvite) : Events()
            data class OnJoined(val topic: String) : Events()
            data class OnReject(val topic: String) : Events()
            data class OnMessage(val message: Message) : Events()
            data class OnLeft(val topic: String) : Events()
        }

        data class Cacao(
            val header: Header,
            val payload: Payload,
            val signature: Signature,
        ) : Model() {
            @Keep
            data class Signature(override val t: String, override val s: String, override val m: String? = null) : Model(), SignatureInterface
            data class Header(val t: String) : Model()
            data class Payload(
                val iss: String,
                val domain: String,
                val aud: String,
                val version: String,
                val nonce: String,
                val iat: String,
                val nbf: String?,
                val exp: String?,
                val statement: String?,
                val requestId: String?,
                val resources: List<String>?,
            ) : Model()
        }
    }

    sealed class Params {
        data class Init(val core: CoreClient, val keyServerUrl: String = DEFUALT_KEYSERVER_URL) : Params()
        data class Resolve(val account: Type.AccountId) : Params()
        data class Invite(val invite: Model.Invite) : Params()
        data class Accept(val inviteId: Long) : Params()
        data class Reject(val inviteId: Long) : Params()
        data class Message(val topic: String, val message: Type.ChatMessage, val media: Model.Media? = null) : Params()
        data class Ping(val topic: String) : Params()
        data class Leave(val topic: String) : Params()
        data class SetContact(val account: Type.AccountId, val publicKey: String) : Params()
        data class GetReceivedInvites(val account: Type.AccountId) : Params()
        data class GetSentInvites(val account: Type.AccountId) : Params()
        data class GetThreads(val account: Type.AccountId) : Params()
        data class GetMessages(val topic: String) : Params()
        data class Register(val account: Type.AccountId, val private: Boolean = false) : Params()
        data class Unregister(val account: Type.AccountId) : Params()
        data class GoPrivate(val account: Type.AccountId) : Params()
        data class GoPublic(val account: Type.AccountId) : Params()
    }

    const val DEFUALT_KEYSERVER_URL = "https://staging.keys.walletconnect.com"
}