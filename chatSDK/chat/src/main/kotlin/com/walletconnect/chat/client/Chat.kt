package com.walletconnect.chat.client

import android.app.Application

object Chat {

    sealed class Model {
        data class Error(val throwable: Throwable) : Model() // TODO: Should this be extracted to core for easier error handling?

        data class Invite(
            val account: String,
            val message: String,
            val signature: String? = "", // TODO: Extract String.Empty to core module and use it here
        ) : Model()

        data class Media(
            val type: String,
            val data: String,
        ) : Model()

        data class Thread(
            // TODO: Define structure in specs
            val topic: String,
        ) : Model()

        data class Message(
            // TODO: Define structure in specs
            val message: String,
        ) : Model()

        sealed class Events : Model() {
            data class OnInvite(val id: Int, val invite: Invite) : Events()

            data class OnJoined(val topic: String) : Events()

            data class OnMessage(val topic: String, val message: String) : Events()

            data class OnLeft(val topic: String) : Events()
        }
    }

    sealed class Params {
        data class Init(val application: Application) : Params()

        data class Register(val account: String, val private: Boolean? = false) : Params()

        data class Resolve(val account: String) : Params()

        data class Invite(val account: String, val invite: Model.Invite) : Params()

        data class Accept(val inviteId: String) : Params()

        data class Reject(val inviteId: String) : Params()

        data class Message(val topic: String, val message: String, val media: Model.Media) : Params()

        data class Ping(val topic: String) : Params()

        data class Leave(val topic: String) : Params()

        data class AddContact(val account: String, val publicKey: String) : Params()

        data class GetInvites(val account: String) : Params()

        data class GetThreads(val account: String) : Params()

        data class GetMessages(val topic: String) : Params()
    }
}