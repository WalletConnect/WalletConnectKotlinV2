package com.walletconnect.walletconnectv2.di

import android.app.Application
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.squareup.sqldelight.ColumnAdapter
import com.squareup.sqldelight.EnumColumnAdapter
import com.squareup.sqldelight.android.AndroidSqliteDriver
import com.squareup.sqldelight.db.SqlDriver
import com.tinder.scarlet.Scarlet
import com.tinder.scarlet.lifecycle.android.AndroidLifecycle
import com.tinder.scarlet.messageadapter.moshi.MoshiMessageAdapter
import com.tinder.scarlet.retry.LinearBackoffStrategy
import com.tinder.scarlet.utils.getRawType
import com.tinder.scarlet.websocket.okhttp.newWebSocketFactory
import com.walletconnect.walletconnectv2.Database
import com.walletconnect.walletconnectv2.common.Expiry
import com.walletconnect.walletconnectv2.common.SubscriptionId
import com.walletconnect.walletconnectv2.common.Topic
import com.walletconnect.walletconnectv2.common.Ttl
import com.walletconnect.walletconnectv2.common.network.adapters.*
import com.walletconnect.walletconnectv2.crypto.codec.Codec
import com.walletconnect.walletconnectv2.crypto.managers.CryptoManager
import com.walletconnect.walletconnectv2.crypto.codec.AuthenticatedEncryptionCodec
import com.walletconnect.walletconnectv2.crypto.managers.BouncyCastleCryptoManager
import com.walletconnect.walletconnectv2.jsonrpc.model.JsonRpcResponse
import com.walletconnect.walletconnectv2.relay.waku.RelayService
import com.walletconnect.walletconnectv2.storage.KeyChain
import com.walletconnect.walletconnectv2.storage.KeyStore
import com.walletconnect.walletconnectv2.util.adapters.FlowStreamAdapter
import dagger.Binds
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import org.json.JSONObject
import org.walletconnect.walletconnectv2.storage.data.dao.MetaDataDao
import org.walletconnect.walletconnectv2.storage.data.dao.PairingDao
import org.walletconnect.walletconnectv2.storage.data.dao.SessionDao
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
interface WCModule {

    @Binds
    fun bindsCryptoManager(bouncyCastleCryptoManager: BouncyCastleCryptoManager): CryptoManager

    @Binds
    fun bindsCodec(authenticatedEncryptionCodec: AuthenticatedEncryptionCodec): Codec

    @Binds
    fun bindsKeyChain(keyStore: KeyChain): KeyStore

    companion object {
        private const val sharedPrefsFileKeyStore: String = "wc_key_store"
        private const val sharedPrefsFileRpcStore: String = "wc_rpc_store"
        private val keyGenParameterSpec = MasterKeys.AES256_GCM_SPEC
        private val mainKeyAlias = MasterKeys.getOrCreate(keyGenParameterSpec)
        private const val TIMEOUT_TIME: Long = 5000L
        private const val DEFAULT_BACKOFF_MINUTES: Long = 5L
        private val listOfStringsAdapter = object : ColumnAdapter<List<String>, String> {

            override fun decode(databaseValue: String) =
                if (databaseValue.isEmpty()) {
                    listOf()
                } else {
                    databaseValue.split(",")
                }

            override fun encode(value: List<String>) = value.joinToString(separator = ",")
        }

        @Singleton
        @Provides
        fun providesSqlDelight(application: Application): SqlDriver = AndroidSqliteDriver(
            schema = Database.Schema,
            context = application,
            name = "WalletConnectV2.db"
        )

        @Singleton
        @Provides
        fun providesSessionDatabase(driver: SqlDriver): Database = Database(
            driver,
            PairingDaoAdapter = PairingDao.Adapter(
                statusAdapter = EnumColumnAdapter(),
                controller_typeAdapter = EnumColumnAdapter()
            ),
            SessionDaoAdapter = SessionDao.Adapter(
                permissions_chainsAdapter = listOfStringsAdapter,
                permissions_methodsAdapter = listOfStringsAdapter,
                permissions_typesAdapter = listOfStringsAdapter,
                accountsAdapter = listOfStringsAdapter,
                statusAdapter = EnumColumnAdapter(),
                controller_typeAdapter = EnumColumnAdapter()
            ),
            MetaDataDaoAdapter = MetaDataDao.Adapter(iconsAdapter = listOfStringsAdapter)
        )

        @Singleton
        @Named("rpcStore")
        @Provides
        fun providesSharedPreferencesRpcStore(application: Application): SharedPreferences =
            EncryptedSharedPreferences.create(
                sharedPrefsFileRpcStore,
                mainKeyAlias,
                application,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )

        @Singleton
        @Named("keyStore")
        @Provides
        fun providesSharedPreferencesKeyStore(application: Application): SharedPreferences =
            EncryptedSharedPreferences.create(
                sharedPrefsFileKeyStore,
                mainKeyAlias,
                application,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )

        @Provides
        fun providesPolymorphicJsonAdapterFactory(): PolymorphicJsonAdapterFactory<JsonRpcResponse> =
            PolymorphicJsonAdapterFactory.of(JsonRpcResponse::class.java, "type")
                .withSubtype(JsonRpcResponse.JsonRpcResult::class.java, "result")
                .withSubtype(JsonRpcResponse.JsonRpcError::class.java, "error")

        @Provides
        fun providesMoshi(polymorphicJsonAdapterFactory: PolymorphicJsonAdapterFactory<JsonRpcResponse>): Moshi = Moshi.Builder()
            .addLast { type, _, _ ->
                when (type.getRawType().name) {
                    Expiry::class.qualifiedName -> ExpiryAdapter
                    JSONObject::class.qualifiedName -> JSONObjectAdapter
                    SubscriptionId::class.qualifiedName -> SubscriptionIdAdapter
                    Topic::class.qualifiedName -> TopicAdapter
                    Ttl::class.qualifiedName -> TtlAdapter
                    else -> null
                }
            }
            .addLast(KotlinJsonAdapterFactory())
            .add(polymorphicJsonAdapterFactory)
            .build()

        @Provides
        fun providesOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
            .writeTimeout(TIMEOUT_TIME, TimeUnit.MILLISECONDS)
            .readTimeout(TIMEOUT_TIME, TimeUnit.MILLISECONDS)
            .callTimeout(TIMEOUT_TIME, TimeUnit.MILLISECONDS)
            .connectTimeout(TIMEOUT_TIME, TimeUnit.MILLISECONDS)
            .build()

        @Named("serverUrl")
        @Provides
        fun providesServerUrl(
            useTLs: Boolean,
            @Named("hostName") hostName: String,
            @Named("projectID") projectID: String
        ): String = ((if (useTLs) "wss" else "ws") + "://$hostName/?projectId=$projectID").trim()

        @Singleton
        @Provides
        fun providesScarlet(
            moshi: Moshi,
            okHttpClient: OkHttpClient,
            @Named("serverUrl") serverUrl: String,
            application: Application
        ): Scarlet = Scarlet.Builder()
            .backoffStrategy(LinearBackoffStrategy(TimeUnit.MINUTES.toMillis(DEFAULT_BACKOFF_MINUTES)))
            .webSocketFactory(okHttpClient.newWebSocketFactory(serverUrl))
            .lifecycle(AndroidLifecycle.ofApplicationForeground(application)) // TODO: Maybe have debug version of scarlet w/o application and release version of scarlet w/ application once DI is setup
            .addMessageAdapterFactory(MoshiMessageAdapter.Factory(moshi))
            .addStreamAdapterFactory(FlowStreamAdapter.Factory())
            .build()

        @Provides
        fun providesRelayService(scarlet: Scarlet): RelayService = scarlet.create(RelayService::class.java)
    }
}