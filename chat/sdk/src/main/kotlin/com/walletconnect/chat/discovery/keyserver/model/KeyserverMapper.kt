@file:JvmSynthetic

package com.walletconnect.chat.discovery.keyserver.model

import com.walletconnect.chat.core.model.vo.AccountIdVO
import com.walletconnect.chat.core.model.vo.AccountIdWithPublicKeyVO
import com.walletconnect.foundation.common.model.PublicKey

@JvmSynthetic
internal fun KeyServerDTO.Account.toVOAccount(): AccountIdWithPublicKeyVO = AccountIdWithPublicKeyVO(AccountIdVO(account), PublicKey(publicKey))

@JvmSynthetic
internal fun AccountIdWithPublicKeyVO.toDTOAccount(): KeyServerDTO.Account = KeyServerDTO.Account(accountId.value, publicKey.keyAsHex)