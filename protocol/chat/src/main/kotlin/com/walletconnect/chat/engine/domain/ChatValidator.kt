package com.walletconnect.chat.engine.domain


internal object ChatValidator {
    fun isInviteMessageValid(value : String) : Boolean = value.length <= MAX_LENGTH_INVITE_MESSAGE
    fun isChatMessageValid(value : String) : Boolean = value.length <= MAX_LENGTH_CHAT_MESSAGE
    fun isMediaDataValid(value : String?) : Boolean = (value?.length ?: 0) <= MAX_LENGTH_MEDIA_DATA

    internal const val MAX_LENGTH_INVITE_MESSAGE = 200
    internal const val MAX_LENGTH_CHAT_MESSAGE = 1000
    internal const val MAX_LENGTH_MEDIA_DATA = 500
}