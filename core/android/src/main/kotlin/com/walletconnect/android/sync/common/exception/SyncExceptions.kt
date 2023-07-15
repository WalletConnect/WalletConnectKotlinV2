package com.walletconnect.android.sync.common.exception

import com.walletconnect.android.internal.common.exception.WalletConnectException
import com.walletconnect.android.internal.common.model.AccountId

internal class InvalidAccountIdException(accountId: AccountId) : WalletConnectException("AccountId: $accountId is not CAIP-10 complaint") // todo: https://github.com/WalletConnect/WalletConnectKotlinV2/issues/768
internal class InvalidSignatureException() : WalletConnectException("Invalid signature")


//todo: Move to com.walletconnect.android.internal.common.model.AccountId as a part of https://github.com/WalletConnect/WalletConnectKotlinV2/issues/768
@JvmSynthetic
internal inline fun validateAccountId(accountId: AccountId, onFailure: (Exception) -> Unit) {
    if (!accountId.isValid()) return onFailure(InvalidAccountIdException(accountId))
}