package com.walletconnect.sync.common.exception

import com.walletconnect.android.internal.common.exception.WalletConnectException
import com.walletconnect.android.internal.common.model.AccountId

internal class InvalidAccountIdException(accountId: AccountId) : WalletConnectException("AccountId: $accountId is not CAIP-10 complaint") // todo: https://github.com/WalletConnect/WalletConnectKotlinV2/issues/768
