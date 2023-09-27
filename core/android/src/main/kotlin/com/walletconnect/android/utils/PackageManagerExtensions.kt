package com.walletconnect.android.utils

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build

fun PackageManager.isWalletInstalled(
    appPackage: String?,
    nativeLink: String?
): Boolean =
    try {
        isPackageInstalled(appPackage!!)
    } catch (e: Exception) {
        canDeeplinkBeResolved(nativeLink)
    }

fun PackageManager.isPackageInstalled(packageName: String): Boolean {
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
        } else {
            getPackageInfo(packageName, 0)
        }
        true
    } catch (e: PackageManager.NameNotFoundException) {
        false
    }
}

private fun PackageManager.canDeeplinkBeResolved(nativeLink: String?): Boolean {
    return try {
            val intent = Intent().apply { data = Uri.parse(nativeLink) }
            intent.resolveActivity(this) != null
        } catch (e: Exception) {
            false
        }
}
