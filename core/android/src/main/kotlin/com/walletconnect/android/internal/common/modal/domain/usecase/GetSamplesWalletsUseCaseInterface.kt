package com.walletconnect.android.internal.common.modal.domain.usecase

import android.content.Context
import com.walletconnect.android.internal.common.modal.data.model.Wallet
import com.walletconnect.android.utils.isWalletInstalled

interface GetSampleWalletsUseCaseInterface {
    suspend operator fun invoke(): List<Wallet>
}

internal class GetSampleWalletsUseCase(
    private val context: Context
) : GetSampleWalletsUseCaseInterface {
    override suspend fun invoke(): List<Wallet> {
        val samples = listOf(SampleWallet, RNSampleWallet)
        samples.forEach { wallet ->
            wallet.apply {
                isWalletInstalled = androidSamplePackages.any { samplePackage -> context.packageManager.isWalletInstalled(samplePackage) }
            }
        }
        return samples.filter { it.isWalletInstalled }
    }
}


private val SampleWallet = Wallet(
    id = "AndroidSampleWallet",
    name = "Android Sample",
    homePage = "https://walletconnect.com",
    imageUrl = "https://raw.githubusercontent.com/WalletConnect/WalletConnectKotlinV2/develop/sample/wallet/src/main/res/drawable-xxxhdpi/wc_icon.png",
    order = "1",
    mobileLink = "kotlin-web3wallet://",
    playStore = null,
    webAppLink = null,
    linkMode = "https://web3modal-laboratory-git-chore-kotlin-assetlinks-walletconnect1.vercel.app/wallet",
    true
)

private val RNSampleWallet = Wallet(
    id = "RNSampleWallet",
    name = "RN Sample",
    homePage = "https://walletconnect.com",
    imageUrl = "https://raw.githubusercontent.com/WalletConnect/WalletConnectKotlinV2/develop/sample/wallet/src/main/res/drawable-xxxhdpi/wc_icon.png",
    order = "2",
    mobileLink = "rn-web3wallet://",
    playStore = null,
    webAppLink = null,
    linkMode = "https://lab.web3modal.com/walletkit_rn",
    true
)

private val androidSamplePackages = listOf(
    "com.walletconnect.sample.wallet",
    "com.walletconnect.sample.wallet.debug",
    "com.walletconnect.sample.wallet.internal",
    "com.walletconnect.web3wallet.rnsample.internal"
)
