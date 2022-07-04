@file:JvmSynthetic

package com.walletconnect.chat.storage

import com.walletconnect.chat.copiedFromSign.core.model.vo.PublicKey
import com.walletconnect.chat.core.model.vo.AccountIdVO
import com.walletconnect.chat.engine.model.EngineDO

internal class ChatStorageRepository(
    private val contactsDao: ContactsDao,
) {
    @JvmSynthetic
    internal fun doesContactNotExists(accountIdVO: AccountIdVO) = contactsDao.doesContactNotExists(accountIdVO.value)

    @JvmSynthetic
    internal fun createContact(contact: EngineDO.Contact) = contactsDao.createContact(
        contact.accountIdWithPublicKeyVO.accountId.value,
        contact.accountIdWithPublicKeyVO.publicKey.keyAsHex,
        contact.displayName
    )

    @JvmSynthetic
    internal fun getContact(accountIdVO: AccountIdVO) = contactsDao.getContact(accountIdVO.value)

    @JvmSynthetic
    internal fun updateContact(accountIdVO: AccountIdVO, publicKey: PublicKey, displayName: String) =
        contactsDao.updateContact(accountIdVO.value, publicKey.keyAsHex, displayName)
}