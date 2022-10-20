@file:JvmSynthetic

package com.walletconnect.chat.discovery.keyserver.model

import com.walletconnect.chat.common.model.AccountId
import com.walletconnect.chat.common.model.AccountIdWithPublicKey
import com.walletconnect.foundation.common.model.PublicKey

@JvmSynthetic
internal fun KeyServerDTO.Account.toVOAccount(): AccountIdWithPublicKey = AccountIdWithPublicKey(AccountId(account), PublicKey(publicKey))

@JvmSynthetic
internal fun AccountIdWithPublicKey.toDTOAccount(): KeyServerDTO.Account = KeyServerDTO.Account(accountId.value, publicKey.keyAsHex)