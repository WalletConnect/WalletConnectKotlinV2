@file:JvmSynthetic

package com.walletconnect.chat.storage

import com.walletconnect.chat.common.model.AccountId
import com.walletconnect.chat.engine.model.EngineDO
import com.walletconnect.chat.storage.data.dao.Contacts
import com.walletconnect.chat.storage.data.dao.ContactsQueries
import com.walletconnect.foundation.common.model.PublicKey

internal class ChatStorageRepository(
    private val contactsQueries: ContactsQueries,

    ) {
    @JvmSynthetic
    internal fun doesContactNotExists(accountIdVO: AccountId): Boolean =
        contactsQueries.doesContactNotExists(accountIdVO.value).executeAsOne()

    @JvmSynthetic
    internal fun createContact(contact: EngineDO.Contact) = contactsQueries.insertOrAbortContact(
        contact.accountIdWithPublicKeyVO.accountId.value,
        contact.accountIdWithPublicKeyVO.publicKey.keyAsHex,
        contact.displayName
    )

    @JvmSynthetic
    internal fun getContact(accountIdVO: AccountId) : Contacts = contactsQueries.getContact(accountIdVO.value).executeAsOne()

    @JvmSynthetic
    internal fun updateContact(accountIdVO: AccountId, publicKey: PublicKey, displayName: String) {
        contactsQueries.updateContactPublicKey(publicKey.keyAsHex, accountIdVO.value)
        contactsQueries.updateContactDisplayName(displayName, accountIdVO.value)
    }
}