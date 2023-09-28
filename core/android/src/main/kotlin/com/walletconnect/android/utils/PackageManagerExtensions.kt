package com.walletconnect.android.utils

import android.content.pm.PackageManager
import android.os.Build

fun PackageManager.isWalletInstalled(
    appPackage: String?,
): Boolean =
    try {
        isPackageInstalled(appPackage!!)
    } catch (e: Exception) {
        false
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
