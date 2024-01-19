package com.walletconnect.android.internal.common.modal

import android.content.Context
import com.walletconnect.android.internal.common.modal.data.model.Wallet
import com.walletconnect.android.internal.common.modal.data.model.WalletAppData
import com.walletconnect.android.internal.common.modal.data.model.WalletListing
import com.walletconnect.android.internal.common.modal.data.network.Web3ModalService
import com.walletconnect.android.internal.common.modal.data.network.model.WalletDTO
import com.walletconnect.android.internal.common.modal.data.network.model.WalletDataDTO
import com.walletconnect.android.utils.isWalletInstalled

internal class Web3ModalApiRepository(
    private val context: Context,
    private val web3ModalApiUrl: String,
    private val web3ModalService: Web3ModalService
) {

    suspend fun getAndroidWalletsData(sdkType: String) = runCatching {
        web3ModalService.getAndroidData(sdkType = sdkType)
    }.mapCatching { response ->
        response.body()!!.data.toWalletsAppData().filter { it.isInstalled }
    }

    suspend fun getWallets(
        sdkType: String,
        page: Int,
        search: String? = null,
        excludeIds: List<String>? = null,
        includeWallets: List<String>? = null
    ) = runCatching {
        web3ModalService.getWallets(
            sdkType = sdkType,
            page = page,
            search = search,
            exclude = excludeIds?.joinToString(","),
            include = includeWallets?.joinToString(",")
        )
    }.mapCatching { response ->
        val body = response.body()!!
        WalletListing(
            page = page,
            totalCount = body.count,
            wallets = body.data.toWallets()
        )
    }

    private fun List<WalletDTO>.toWallets(): List<Wallet> = map { walletDTO ->
        Wallet(
            id = walletDTO.id,
            name = walletDTO.name,
            homePage = walletDTO.homePage,
            imageUrl = web3ModalApiUrl + "getWalletImage/${walletDTO.imageId}",
            order = walletDTO.order,
            mobileLink = walletDTO.mobileLink,
            playStore = walletDTO.playStore,
            webAppLink = walletDTO.webappLink,
        ).apply {
            isWalletInstalled = context.packageManager.isWalletInstalled(appPackage)
        }
    }

    private fun List<WalletDataDTO>.toWalletsAppData() = map { data ->
        WalletAppData(
            id = data.id,
            appPackage = data.appId,
            isInstalled = context.packageManager.isWalletInstalled(data.appId)
        )
    }

    private fun WalletListing.addSampleWalletsIfNeeded(addSamples: Boolean): WalletListing {
        if (!addSamples) {
            return this
        }
        val samples = listOf(SampleDebugWallet, SampleWallet, SampleWalletInternal)
        samples.forEach { wallet ->
            wallet.apply {
                isWalletInstalled = context.packageManager.isWalletInstalled(appPackage)
            }
        }
        val installedSamples = samples.filter { it.isWalletInstalled }
        return this.copy(
            totalCount = this.totalCount + installedSamples.size,
            wallets = installedSamples + this.wallets
        )
    }
}

private val SampleDebugWallet = Wallet(
    id = "AndroidSampleWalletDebug${generateId()}",
    name = "Android Sample Debug",
    homePage = "https://walletconnect.com",
    imageUrl = "https://github.com/WalletConnect/walletconnect-assets/blob/master/Icon/Gradient/Icon.png",
    order = "1",
    mobileLink = "wc://",
    playStore = null,
    webAppLink = null,
    true
).apply {
    appPackage = "com.walletconnect.sample.wallet.debug"
}

private val SampleWallet = Wallet(
    id = "AndroidSampleWallet${generateId()}",
    name = "Android Sample",
    homePage = "https://walletconnect.com",
    imageUrl = "https://github.com/WalletConnect/walletconnect-assets/blob/master/Icon/Gradient/Icon.png",
    order = "1",
    mobileLink = "wc://",
    playStore = null,
    webAppLink = null,
    true
).apply {
    appPackage = "com.walletconnect.sample.wallet"
}

private val SampleWalletInternal = Wallet(
    id = "AndroidSampleWalletDebugInternal${generateId()}",
    name = "Android Sample Internal",
    homePage = "https://walletconnect.com",
    imageUrl = "https://github.com/WalletConnect/walletconnect-assets/blob/master/Icon/Gradient/Icon.png",
    order = "1",
    mobileLink = "wc://",
    playStore = null,
    webAppLink = null,
    true
).apply {
    appPackage = "com.walletconnect.sample.wallet.internal"
}

//<package android:name="com.walletconnect.sample.wallet" />
//<package android:name="com.walletconnect.sample.wallet.debug" />
//<package android:name="com.walletconnect.sample.wallet.internal" />