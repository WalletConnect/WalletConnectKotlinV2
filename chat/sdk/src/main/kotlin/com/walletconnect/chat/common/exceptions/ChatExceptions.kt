package com.walletconnect.chat.common.exceptions

import com.walletconnect.android.internal.common.exception.WalletConnectException
import com.walletconnect.chat.engine.domain.ChatValidator.MAX_LENGTH_CHAT_MESSAGE
import com.walletconnect.chat.engine.domain.ChatValidator.MAX_LENGTH_INVITE_MESSAGE
import com.walletconnect.chat.engine.domain.ChatValidator.MAX_LENGTH_MEDIA_DATA

class InvalidAccountIdException(override val message: String?) : WalletConnectException(message)
class UnableToExtractDomainException(override val message: String?) : WalletConnectException(message)
class InviteKeyNotFound(override val message: String?) : WalletConnectException(message)
object AccountsAlreadyHaveInviteException : WalletConnectException("Accounts already have invites")
class InviteMessageTooLongException : WalletConnectException("Invite message max length is $MAX_LENGTH_INVITE_MESSAGE")
class ChatMessageTooLongException : WalletConnectException("Chat message max length is $MAX_LENGTH_CHAT_MESSAGE")
class MediaDataTooLongException : WalletConnectException("Media data max length is $MAX_LENGTH_MEDIA_DATA")
object MissingInviteRequestException: WalletConnectException("Missing Invite Request")
