package com.walletconnect.notify.client

import com.walletconnect.android.internal.common.exception.WalletConnectException

class InvalidDidJsonFileException(override val message: String?) : WalletConnectException(message)
class AccountIsNotRegisteredException(val account: String) : WalletConnectException("Account: $account is not registered")
class AccountIsNotRegisteredForAppException(val account: String) : WalletConnectException("Account: $account is not registered for this app")
class AccountIsNotRegisteredForAllAppsException(val account: String) : WalletConnectException("Account: $account is not registered for all apps")
class AccountIsRegisteredForAllAppsException(val account: String) : WalletConnectException("Account: $account is registered for all apps")
class AccountIsMissingIdentityKeysException(val account: String) : WalletConnectException("Account: $account is missing identity keys")