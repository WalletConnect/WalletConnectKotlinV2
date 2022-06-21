@file:JvmSynthetic

package com.walletconnect.chat.discovery.keyserver.model

import com.walletconnect.chat.copiedFromSign.core.model.vo.PublicKey
import com.walletconnect.chat.core.model.vo.AccountId
import com.walletconnect.chat.core.model.vo.AccountIdWithPublicKeyVO

@JvmSynthetic
internal fun KeyServerDTO.Account.toVOAccount(): AccountIdWithPublicKeyVO = AccountIdWithPublicKeyVO(AccountId(account), PublicKey(publicKey))

@JvmSynthetic
internal fun AccountIdWithPublicKeyVO.toDTOAccount(): KeyServerDTO.Account = KeyServerDTO.Account(accountId.value, publicKey.keyAsHex)
