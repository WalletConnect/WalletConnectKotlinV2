@file:JvmSynthetic

package com.walletconnect.auth.common.exceptions

import com.walletconnect.android.internal.common.exception.WalletConnectException

internal object MissingAuthRequestException : WalletConnectException(MISSING_AUTH_REQUEST_MESSAGE)
internal object InvalidCacaoException : WalletConnectException(CACAO_IS_NOT_VALID_MESSAGE)
internal class InvalidParamsException(override val message: String) : WalletConnectException(message)
