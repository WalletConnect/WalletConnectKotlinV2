package com.walletconnect.chat.common.exceptions

import com.walletconnect.android.internal.common.exception.WalletConnectException
import com.walletconnect.chat.engine.domain.ChatValidator.MAX_LENGTH_CHAT_MESSAGE
import com.walletconnect.chat.engine.domain.ChatValidator.MAX_LENGTH_INVITE_MESSAGE
import com.walletconnect.chat.engine.domain.ChatValidator.MAX_LENGTH_MEDIA_DATA

class InvalidAccountIdException(override val message: String?) : WalletConnectException(message) // todo: https://github.com/WalletConnect/WalletConnectKotlinV2/issues/768
class InviteKeyNotFound(override val message: String?) : WalletConnectException(message)
object AccountsAlreadyHaveInviteException : WalletConnectException("Accounts already have invites")
class InviteMessageTooLongException : WalletConnectException("Invite message max length is $MAX_LENGTH_INVITE_MESSAGE")
class ChatMessageTooLongException : WalletConnectException("Chat message max length is $MAX_LENGTH_CHAT_MESSAGE")
class MediaDataTooLongException : WalletConnectException("Media data max length is $MAX_LENGTH_MEDIA_DATA")
object MissingInviteRequestException : WalletConnectException("Missing Invite Request")
class InvalidActClaims(act: String) : WalletConnectException("Invalid act claim. Must be equal to $act")
object InviteWasAlreadyRespondedTo : WalletConnectException("This invite request has already been responded to")
object InviteResponseWasAlreadyReceived : WalletConnectException("This invite response has already been received")
object ChatSyncStoresInitializationTimeoutException : WalletConnectException("Required Chat Stores initialization timeout")
object ReceivedInviteNotStored : WalletConnectException("Received Invite not stored")
