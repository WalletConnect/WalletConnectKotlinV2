package com.walletconnect.sign.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@ExperimentalCoroutinesApi
class CoroutineTestRule(val testDispatcher: TestCoroutineDispatcher = TestCoroutineDispatcher()) : TestWatcher() {

    override fun finished(description: Description?) {
        super.finished(description)
        Dispatchers.resetMain()
        testDispatcher.cleanupTestCoroutines()
    }

    override fun starting(description: Description?) {
        super.starting(description)
        Dispatchers.setMain(testDispatcher)
    }
}

@ExperimentalCoroutinesApi
fun CoroutineTestRule.runTest(testBlock: suspend () -> Unit) {
    val job = Job()
    val testCoroutineScope = TestCoroutineScope(job + this.testDispatcher)

    this.testDispatcher.runBlockingTest {
        launch(testCoroutineScope.coroutineContext) {
            testBlock()
            job.complete()
        }
    }
}