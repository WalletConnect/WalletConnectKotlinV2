package com.walletconnect.android.internal.common.di

import org.koin.android.ext.koin.androidContext
import org.koin.core.scope.Scope

object DBUtils {
    const val ANDROID_CORE_DB_NAME = "WalletConnectAndroidCore.db"
    const val SIGN_SDK_DB_NAME = "WalletConnectV2.db"
    const val CHAT_SDK_DB_NAME = "WalletConnectV2_chat.db"

    val dbNames: List<String> = listOf(ANDROID_CORE_DB_NAME, SIGN_SDK_DB_NAME, CHAT_SDK_DB_NAME)
}

fun Scope.deleteDatabase(dbName: String) {
    androidContext().deleteDatabase(dbName)
}

fun Scope.deleteDatabases() {
    androidContext().databaseList().forEach { dbName ->
        if (DBUtils.dbNames.contains(dbName)) {
            deleteDatabase(dbName)
        }
    }
}