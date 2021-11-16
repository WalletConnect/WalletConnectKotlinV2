package org.walletconnect.walletconnectv2

import android.content.Intent
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import org.junit.Assert
import org.junit.rules.ExternalResource
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

// Source: https://gist.github.com/zawadz88/f057c70d3061454207ccd56e0add81c6#file-lazyactivityscenariorule-kt
class WCIntegrationActivityScenarioRule : ExternalResource {
    private var scenarioSupplier: () -> ActivityScenario<IntegrationTestActivity>
    private var scenario: ActivityScenario<IntegrationTestActivity>? = null
    private var scenarioLaunched: Boolean = false
    private val latch = CountDownLatch(1)

    constructor(startActivityIntentSupplier: () -> Intent) {
        scenarioSupplier = { ActivityScenario.launch(startActivityIntentSupplier()) }
    }

    constructor(startActivityIntent: Intent) {
        scenarioSupplier = { ActivityScenario.launch(startActivityIntent) }
    }

    constructor() {
        scenarioSupplier = { ActivityScenario.launch(IntegrationTestActivity::class.java) }
    }

    override fun before() {
    }

    override fun after() {
        scenario?.close()
    }

    fun launch(newIntent: Intent? = null, timeoutMinutes: Long = 1, testCodeBlock: () -> Unit) {
        require(!scenarioLaunched) { "Scenario has already been launched!" }

        newIntent?.let { scenarioSupplier = { ActivityScenario.launch(it) } }

        scenario = scenarioSupplier()
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

fun wcActivityScenarioRule(intentSupplier: () -> Intent): WCIntegrationActivityScenarioRule =
    WCIntegrationActivityScenarioRule(intentSupplier)

fun wcActivityScenarioRule(intent: Intent? = null): WCIntegrationActivityScenarioRule = if (intent == null) {
    WCIntegrationActivityScenarioRule()
} else {
    WCIntegrationActivityScenarioRule(intent)
}
