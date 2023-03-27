@file:JvmSynthetic

package com.walletconnect.chat.storage

import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.chat.common.model.Contact
import com.walletconnect.chat.storage.data.dao.ContactsQueries
import com.walletconnect.foundation.common.model.PublicKey

internal class ContactStorageRepository(private val contactsQueries: ContactsQueries) {

    @JvmSynthetic
    internal suspend fun doesContactNotExists(accountIdVO: AccountId): Boolean =
        contactsQueries.doesContactNotExists(accountIdVO.value).executeAsOne()

    @JvmSynthetic
    internal suspend fun upsertContact(contact: Contact) = with(contact) {
        if (doesContactNotExists(accountId)) {
            createContact(contact)
        } else {
            updateContact(contact)
        }
    }

    @JvmSynthetic
    internal suspend fun createContact(contact: Contact) = contactsQueries.insertOrAbortContact(
        contact.accountId.value,
        contact.publicKey.keyAsHex,
        contact.displayName
    )

    @JvmSynthetic
    internal suspend fun getContact(accountId: AccountId): Contact =
        contactsQueries.getContact(accountId.value, mapper = ::mapContactDaoToContact).executeAsOne()

    @JvmSynthetic
    internal suspend fun updateContact(contact: Contact) = with(contact) {
        contactsQueries.updateContactPublicKey(publicKey.keyAsHex, accountId.value)
        contactsQueries.updateContactDisplayName(displayName, accountId.value)
    }

    private fun mapContactDaoToContact(account_id: String, public_key: String, display_name: String): Contact =
        Contact(AccountId(account_id), PublicKey(public_key), display_name)
}