package com.walletconnect.android.internal.common.di

import com.walletconnect.utils.Empty
import org.koin.android.ext.koin.androidContext
import org.koin.core.scope.Scope

class DatabaseConfig(var storagePrefix: String = String.Empty) {
    val ANDROID_CORE_DB_NAME
        get() = storagePrefix + "WalletConnectAndroidCore.db"

    val SIGN_SDK_DB_NAME
        get() = storagePrefix + "WalletConnectV2.db"

    val CHAT_SDK_DB_NAME
        get() = storagePrefix + "WalletConnectV2_chat.db"

    val PUSH_DAPP_SDK_DB_NAME
        get() = storagePrefix + "WalletConnectV2_dapp_push.db"

    val PUSH_WALLET_SDK_DB_NAME
        get() = storagePrefix + "WalletConnectV2_wallet_push.db"

    val dbNames: List<String> = listOf(ANDROID_CORE_DB_NAME, SIGN_SDK_DB_NAME, CHAT_SDK_DB_NAME, PUSH_DAPP_SDK_DB_NAME, PUSH_WALLET_SDK_DB_NAME)
}

fun Scope.deleteDatabase(dbName: String) {
    androidContext().deleteDatabase(dbName)
}

fun Scope.deleteDatabases() {
    androidContext().databaseList().forEach { dbName ->
        if (get<DatabaseConfig>().dbNames.contains(dbName)) {
            deleteDatabase(dbName)
        }
    }
}