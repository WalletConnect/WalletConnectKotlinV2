package org.walletconnect.walletconnectv2

import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import org.junit.Assert
import org.junit.rules.ExternalResource
import org.walletconnect.walletconnectv2.utils.IntegrationTestActivity
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

// Source: https://gist.github.com/zawadz88/f057c70d3061454207ccd56e0add81c6#file-lazyactivityscenariorule-kt
class WCIntegrationActivityScenarioRule : ExternalResource() {
    private var scenario: ActivityScenario<IntegrationTestActivity>? = null
    private var scenarioLaunched: Boolean = false
    private val latch = CountDownLatch(1)

    override fun before() {
    }

    override fun after() {
        scenario?.close()
    }

    fun launch(timeoutMinutes: Long = 1, testCodeBlock: () -> Unit) {
        require(!scenarioLaunched) { "Scenario has already been launched!" }

        scenario = ActivityScenario.launch(IntegrationTestActivity::class.java)
        scenarioLaunched = true

        scenario?.moveToState(Lifecycle.State.RESUMED)
        assert(scenario?.state?.isAtLeast(Lifecycle.State.RESUMED) == true)

        testCodeBlock()

        try {
            latch.await(timeoutMinutes, TimeUnit.MINUTES)
        } catch (exception: InterruptedException) {
            Assert.fail(exception.stackTraceToString())
        } catch (exception: IllegalArgumentException) {
            Assert.fail(exception.stackTraceToString())
        }
    }

    fun close() {
        latch.countDown()
    }
}