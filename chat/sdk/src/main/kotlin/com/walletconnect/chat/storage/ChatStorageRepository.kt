@file:JvmSynthetic

package com.walletconnect.chat.storage

import com.walletconnect.chat.common.model.AccountId
import com.walletconnect.chat.common.model.Contact
import com.walletconnect.chat.engine.model.EngineDO
import com.walletconnect.chat.storage.data.dao.ContactsQueries
import com.walletconnect.foundation.common.model.PublicKey

internal class ChatStorageRepository(private val contactsQueries: ContactsQueries) {

    @JvmSynthetic
    internal fun doesContactNotExists(accountIdVO: AccountId): Boolean =
        contactsQueries.doesContactNotExists(accountIdVO.value).executeAsOne()

    @JvmSynthetic
    internal fun createContact(contact: EngineDO.Contact) =
        contactsQueries.insertOrAbortContact(
            contact.accountIdWithPublicKey.accountId.value,
            contact.accountIdWithPublicKey.publicKey.keyAsHex,
            contact.displayName
        )

    @JvmSynthetic
    internal fun getContact(accountId: AccountId): Contact =
        contactsQueries.getContact(accountId.value,  mapper = ::mapContactDaoToContact).executeAsOne()

    @JvmSynthetic
    internal fun updateContact(accountId: AccountId, publicKey: PublicKey, displayName: String) {
        contactsQueries.updateContactPublicKey(publicKey.keyAsHex, accountId.value)
        contactsQueries.updateContactDisplayName(displayName, accountId.value)
    }

    private fun mapContactDaoToContact(account_id: String, public_key: String, display_name: String): Contact =
        Contact(AccountId(account_id), PublicKey(public_key), display_name)
}