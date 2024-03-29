@file:JvmSynthetic

package com.walletconnect.notify.data.jwt.watchSubscriptions

import com.walletconnect.android.internal.common.jwt.did.EncodeDidJwtPayloadUseCase
import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.foundation.common.model.PublicKey
import com.walletconnect.foundation.util.jwt.encodeDidPkh
import com.walletconnect.foundation.util.jwt.encodeDidWeb
import com.walletconnect.foundation.util.jwt.encodeEd25519DidKey

internal class EncodeWatchSubscriptionsRequestJwtUseCase(
    private val accountId: AccountId,
    private val authenticationKey: PublicKey,
    private val appDomain: String?,
) : EncodeDidJwtPayloadUseCase<WatchSubscriptionsRequestJwtClaim> {

    override fun invoke(params: EncodeDidJwtPayloadUseCase.Params): WatchSubscriptionsRequestJwtClaim = with(params) {
        WatchSubscriptionsRequestJwtClaim(
            issuedAt = issuedAt,
            expiration = expiration,
            issuer = issuer,
            keyserverUrl = keyserverUrl,
            audience = encodeEd25519DidKey(authenticationKey.keyAsBytes),
            subject = encodeDidPkh(accountId.value),
            appDidWeb = appDomain?.let { encodeDidWeb(it) }
        )
    }
}