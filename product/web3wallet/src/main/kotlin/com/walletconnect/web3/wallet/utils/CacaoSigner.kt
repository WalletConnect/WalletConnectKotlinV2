@file:JvmSynthetic

package com.walletconnect.web3.wallet.utils

import com.walletconnect.android.cacao.signature.ISignatureType
import com.walletconnect.android.utils.cacao.CacaoSignerInterface
import com.walletconnect.web3.wallet.client.Wallet


/// Only added to have backwards compatibility. Newer SDKs should only add CacaoSigner object below.
@Deprecated("Moved to android-core module, as other SDKs also need CACAO.", ReplaceWith("com.walletconnect.android.internal.common.cacao.signature.SignatureType"))
enum class SignatureType(override val header: String) : ISignatureType {
    EIP191("eip191"), EIP1271("eip1271");
}


@Deprecated("com.walletconnect.web3.wallet.utils.CacaoSigner has been deprecated. Please use com.reown.walletkit.utils.CacaoSigner instead from - https://github.com/reown-com/reown-kotlin")
object CacaoSigner : CacaoSignerInterface<Wallet.Model.Cacao.Signature>