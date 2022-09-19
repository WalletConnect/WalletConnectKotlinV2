package com.walletconnect.sign.storage

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver
import com.walletconnect.sign.Database
import com.walletconnect.sign.util.CoroutineTestRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
internal class StorageRepositoryTest {

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    private val app = ApplicationProvider.getApplicationContext<Application>()
    private val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).apply {
        Database.Schema.create(this)
    }
}