package com.walletconnect.web3.inbox.json_rpc

internal object Web3InboxMethods {
    object Request {
        object Chat {
            // TODO: add chat methods here
        }
        @get:JvmSynthetic
        const val REGISTER: String = "register"

        @get:JvmSynthetic
        const val RESOLVE: String = "resolve"

        @get:JvmSynthetic
        const val GET_RECEIVED_INVITES: String = "getReceivedInvites"

        @get:JvmSynthetic
        const val GET_SENT_INVITES: String = "getSentInvites"

        @get:JvmSynthetic
        const val GET_THREADS: String = "getThreads"

        @get:JvmSynthetic
        const val GET_MESSAGES: String = "getMessages"

        @get:JvmSynthetic
        const val MESSAGE: String = "message"

        @get:JvmSynthetic
        const val ACCEPT: String = "accept"

        @get:JvmSynthetic
        const val REJECT: String = "reject"

        @get:JvmSynthetic
        const val INVITE: String = "invite"

        object Push {
            @get:JvmSynthetic
            const val GET_ACTIVE_SUBSCRIPTIONS: String = "getActiveSubscriptions"

            @get:JvmSynthetic
            const val APPROVE: String = "approve"
        }
    }

    object Call {
        object Chat {
            @get:JvmSynthetic
            const val INVITE: String = "chat_invite"

            @get:JvmSynthetic
            const val INVITE_ACCEPTED: String = "chat_invite_accepted"

            @get:JvmSynthetic
            const val INVITE_REJECTED: String = "chat_invite_rejected"

            @get:JvmSynthetic
            const val MESSAGE: String = "chat_message"

            @get:JvmSynthetic
            const val LEAVE: String = "chat_leave"
        }

        object Push {
            @get:JvmSynthetic
            const val REQUEST: String = "push_request"

            @get:JvmSynthetic
            const val RESPONSE: String = "push_response"

            @get:JvmSynthetic
            const val MESSAGE: String = "push_message"

            @get:JvmSynthetic
            const val SUBSCRIPTION: String = "push_subscription"

            @get:JvmSynthetic
            const val UPDATE: String = "push_update"

            @get:JvmSynthetic
            const val DELETE: String = "push_delete"
        }
    }
}