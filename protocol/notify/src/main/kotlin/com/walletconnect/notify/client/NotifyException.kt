package com.walletconnect.notify.client

import com.walletconnect.android.internal.common.exception.WalletConnectException

@Deprecated("com.walletconnect.notify.client.NotifyClient has been deprecated. Please use com.reown.notify.client.NotifyClient instead from - https://github.com/reown-com/reown-kotlin")
class InvalidDidJsonFileException(override val message: String?) : WalletConnectException(message)
@Deprecated("com.walletconnect.notify.client.NotifyClient has been deprecated. Please use com.reown.notify.client.NotifyClient instead from - https://github.com/reown-com/reown-kotlin")
class AccountIsNotRegisteredException(val account: String) : WalletConnectException("Account: $account is not registered")
@Deprecated("com.walletconnect.notify.client.NotifyClient has been deprecated. Please use com.reown.notify.client.NotifyClient instead from - https://github.com/reown-com/reown-kotlin")
class AccountIsNotRegisteredForAppException(val account: String) : WalletConnectException("Account: $account is not registered for this app")
@Deprecated("com.walletconnect.notify.client.NotifyClient has been deprecated. Please use com.reown.notify.client.NotifyClient instead from - https://github.com/reown-com/reown-kotlin")
class AccountIsNotRegisteredForAllAppsException(val account: String) : WalletConnectException("Account: $account is not registered for all apps")
@Deprecated("com.walletconnect.notify.client.NotifyClient has been deprecated. Please use com.reown.notify.client.NotifyClient instead from - https://github.com/reown-com/reown-kotlin")
class AccountIsRegisteredForAllAppsException(val account: String) : WalletConnectException("Account: $account is registered for all apps")
@Deprecated("com.walletconnect.notify.client.NotifyClient has been deprecated. Please use com.reown.notify.client.NotifyClient instead from - https://github.com/reown-com/reown-kotlin")
class AccountIsMissingIdentityKeysException(val account: String) : WalletConnectException("Account: $account is missing identity keys")