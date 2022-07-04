@file:JvmSynthetic

package com.walletconnect.chat.storage

import com.walletconnect.chat.storage.data.dao.Contacts
import com.walletconnect.chat.storage.data.dao.ContactsQueries

internal class ContactsDao(
    private val contactsQueries: ContactsQueries,
) {
    @JvmSynthetic
    internal fun doesContactNotExists(accountId: String) =
        contactsQueries.doesContactNotExists(accountId).executeAsOne()

    @JvmSynthetic
    internal fun createContact(accountId: String, publicKey: String, displayName: String) =
        contactsQueries.insertOrAbortContact(accountId, publicKey, displayName)

    @JvmSynthetic
    internal fun getContact(accountId: String): Contacts =
        contactsQueries.getContact(accountId).executeAsOne()

    @JvmSynthetic
    internal fun updateContact(accountId: String, publicKey: String, displayName: String) =
        contactsQueries.updateContact(publicKey, displayName, accountId)
}