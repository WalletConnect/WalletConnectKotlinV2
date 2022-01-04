package com.walletconnect.walletconnectv2.storage

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver
import com.walletconnect.walletconnectv2.storage.sequence.StorageRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.walletconnect.walletconnectv2.Database
import com.walletconnect.walletconnectv2.util.CoroutineTestRule

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
internal class StorageRepositoryTest {

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    private val app = ApplicationProvider.getApplicationContext<Application>()
    private val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).apply {
        Database.Schema.create(this)
    }
    private val storageRepository = StorageRepository(driver, app)
}