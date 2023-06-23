@file:JvmSynthetic

package com.walletconnect.push.common.data.storage

import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.android.internal.common.model.AppMetaData
import com.walletconnect.android.internal.common.model.Redirect
import com.walletconnect.android.internal.common.model.RelayProtocolOptions
import com.walletconnect.android.sdk.storage.data.dao.MetaDataQueries
import com.walletconnect.foundation.common.model.PublicKey
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.push.common.model.EngineDO
import com.walletconnect.push.common.storage.data.dao.ProposalQueries
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class ProposalStorageRepository(
    private val proposalQueries: ProposalQueries,
    private val metaDataQueries: MetaDataQueries,
) {

    suspend fun insertProposal(requestId: Long, proposalTopic: String, dappPublicKeyAsHex: String, dappMetaDataId: Long, accountId: String) = withContext(Dispatchers.IO) {
        proposalQueries.insertOrIgnoreProposal(requestId, proposalTopic, dappPublicKeyAsHex, accountId, dappMetaDataId)
    }

    suspend fun getProposalByRequestId(requestId: Long): EngineDO.PushPropose? = withContext(Dispatchers.IO) {
        proposalQueries.getProposalByRequestId(requestId, mapper = ::toPushPropose).executeAsOneOrNull()
    }

    private fun toPushPropose(
        requestId: Long,
        push_propose_topic: String,
        dapp_public_key_as_hex: String,
        accountId: String,
        dappMetaDataId: Long,
    ): EngineDO.PushPropose {
        val dappMetaData = metaDataQueries.getMetadataById(dappMetaDataId, mapper = { name, description, url, icons, native ->
            AppMetaData(
                name = name,
                description = description,
                url = url,
                icons = icons,
                redirect = Redirect(native = native),
            )
        }).executeAsOne()

        return EngineDO.PushPropose(
            requestId = requestId,
            proposalTopic = Topic(push_propose_topic),
            dappPublicKey = PublicKey(dapp_public_key_as_hex),
            accountId = AccountId(accountId),
            relayProtocolOptions = RelayProtocolOptions(),
            dappMetaData = dappMetaData,
        )
    }
}