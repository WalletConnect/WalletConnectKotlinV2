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
        val samples = mapOf(
            "com.walletconnect.sample.wallet.debug" to SampleWalletDebug,
            "com.walletconnect.sample.wallet.internal" to SampleWalletInternal,
            "com.walletconnect.sample.wallet" to SampleWalletRelease,
            "com.walletconnect.web3wallet.rnsample.internal" to RNSampleWallet
        )
        samples.forEach { (walletPackage, wallet) ->
            wallet.apply {
                isWalletInstalled = context.packageManager.isWalletInstalled(walletPackage)
            }
        }
        return samples.map { it.value }.filter { it.isWalletInstalled }
    }
}


private val SampleWalletDebug = Wallet(
    id = "SampleWalletDebug",
    name = "Android Sample Debug",
    homePage = "https://walletconnect.com",
    imageUrl = "https://raw.githubusercontent.com/WalletConnect/WalletConnectKotlinV2/develop/sample/wallet/src/main/res/drawable-xxxhdpi/wc_icon.png",
    order = "1",
    mobileLink = "kotlin-web3wallet://",
    playStore = null,
    webAppLink = null,
    linkMode = "https://web3modal-laboratory-git-chore-kotlin-assetlinks-walletconnect1.vercel.app/wallet_debug",
    true
)

private val SampleWalletInternal = Wallet(
    id = "SampleWalletInternal",
    name = "Android Sample Internal",
    homePage = "https://walletconnect.com",
    imageUrl = "https://raw.githubusercontent.com/WalletConnect/WalletConnectKotlinV2/develop/sample/wallet/src/main/res/drawable-xxxhdpi/wc_icon.png",
    order = "2",
    mobileLink = "kotlin-web3wallet://",
    playStore = null,
    webAppLink = null,
    linkMode = "https://web3modal-laboratory-git-chore-kotlin-assetlinks-walletconnect1.vercel.app/wallet_internal",
    true
)

private val SampleWalletRelease = Wallet(
    id = "SampleWalletRelease",
    name = "Android Sample Release",
    homePage = "https://walletconnect.com",
    imageUrl = "https://raw.githubusercontent.com/WalletConnect/WalletConnectKotlinV2/develop/sample/wallet/src/main/res/drawable-xxxhdpi/wc_icon.png",
    order = "3",
    mobileLink = "kotlin-web3wallet://",
    playStore = null,
    webAppLink = null,
    linkMode = "https://web3modal-laboratory-git-chore-kotlin-assetlinks-walletconnect1.vercel.app/wallet_release",
    true
)

private val RNSampleWallet = Wallet(
    id = "RNSampleWallet",
    name = "RN Sample",
    homePage = "https://walletconnect.com",
    imageUrl = "https://raw.githubusercontent.com/WalletConnect/WalletConnectKotlinV2/develop/sample/wallet/src/main/res/drawable-xxxhdpi/wc_icon.png",
    order = "4",
    mobileLink = "rn-web3wallet://",
    playStore = null,
    webAppLink = null,
    linkMode = "https://lab.web3modal.com/rn_walletkit",
    true
)