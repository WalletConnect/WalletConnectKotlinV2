package com.walletconnect.android.internal.common.explorer

import androidx.core.net.toUri
import com.walletconnect.android.internal.common.explorer.data.model.App
import com.walletconnect.android.internal.common.explorer.data.model.Colors
import com.walletconnect.android.internal.common.explorer.data.model.DappListings
import com.walletconnect.android.internal.common.explorer.data.model.Desktop
import com.walletconnect.android.internal.common.explorer.data.model.ImageUrl
import com.walletconnect.android.internal.common.explorer.data.model.Injected
import com.walletconnect.android.internal.common.explorer.data.model.Listing
import com.walletconnect.android.internal.common.explorer.data.model.Metadata
import com.walletconnect.android.internal.common.explorer.data.model.Mobile
import com.walletconnect.android.internal.common.explorer.data.model.SupportedStandard
import com.walletconnect.android.internal.common.explorer.data.network.ExplorerService
import com.walletconnect.android.internal.common.explorer.data.network.model.AppDTO
import com.walletconnect.android.internal.common.explorer.data.network.model.ColorsDTO
import com.walletconnect.android.internal.common.explorer.data.network.model.DappListingsDTO
import com.walletconnect.android.internal.common.explorer.data.network.model.DesktopDTO
import com.walletconnect.android.internal.common.explorer.data.network.model.ImageUrlDTO
import com.walletconnect.android.internal.common.explorer.data.network.model.InjectedDTO
import com.walletconnect.android.internal.common.explorer.data.network.model.ListingDTO
import com.walletconnect.android.internal.common.explorer.data.network.model.MetadataDTO
import com.walletconnect.android.internal.common.explorer.data.network.model.MobileDTO
import com.walletconnect.android.internal.common.explorer.data.network.model.SupportedStandardDTO
import com.walletconnect.android.internal.common.model.ProjectId
import com.walletconnect.android.internal.common.wcKoinApp

class ExplorerRepository(private val explorerService: ExplorerService) {
    private val projectId by lazy { wcKoinApp.koin.get<ProjectId>() }

    suspend fun getAllDapps(): DappListings {
        return with(explorerService.getAllDapps(projectId.value)) {
            if (isSuccessful && body() != null) {
                body()!!.toDappListing()
            } else {
                throw Throwable(errorBody()?.string())
            }
        }
    }

    private fun DappListingsDTO.toDappListing(): DappListings {
        return DappListings(
            listings = listings.values.map { it.toListing() },
            count = count,
            total = total
        )
    }

    private fun ListingDTO.toListing(): Listing = Listing(
        id = id,
        name = name,
        description = description,
        homepage = homepage.toUri(),
        chains = chains,
        versions = versions,
        sdks = sdks,
        appType = appType,
        imageId = imageId,
        imageUrl = imageUrl.toImageUrl(),
        app = app.toApp(),
        injected = injected?.map { it.toInjected() },
        mobile = mobile.toMobile(),
        desktop = desktop.toDesktop(),
        supportedStandards = supportedStandards.map { it.toSupportedStandard() },
        metadata = metadata.toMetadata(),
        updatedAt = updatedAt
    )

    private fun ImageUrlDTO.toImageUrl(): ImageUrl = ImageUrl(
        sm = sm,
        md = md,
        lg = lg,
    )

    private fun AppDTO.toApp(): App = App(
        browser = browser,
        ios = ios,
        android = android,
        mac = mac,
        windows = windows,
        linux = linux,
        chrome = chrome,
        firefox = firefox,
        safari = safari,
        edge = edge,
        opera = opera
    )

    private fun InjectedDTO.toInjected(): Injected = Injected(
        namespace = namespace,
        injectedId = injectedId
    )

    private fun MobileDTO.toMobile(): Mobile = Mobile(
        native = native,
        universal = universal,
    )

    private fun DesktopDTO.toDesktop(): Desktop = Desktop(
        native = native,
        universal = universal,
    )

    private fun SupportedStandardDTO.toSupportedStandard(): SupportedStandard = SupportedStandard(
        id = id,
        url = url,
        title = title,
        standardId = standardId,
        standardPrefix = standardPrefix
    )

    private fun MetadataDTO.toMetadata(): Metadata = Metadata(
        shortName = shortName,
        colors = colors.toColors(),
    )

    private fun ColorsDTO.toColors(): Colors = Colors(
        primary = primary,
        secondary = secondary
    )
}