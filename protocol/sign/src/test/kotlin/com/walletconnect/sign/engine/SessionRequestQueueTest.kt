package com.walletconnect.sign.engine

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Test
import java.util.LinkedList
import java.util.concurrent.ConcurrentLinkedQueue

class SessionRequestQueueTest {

    @Test
    fun testLinkedListAccessFromMultipleCoroutines() {
        assertThrows(ConcurrentModificationException::class.java) {
            runTest {
                val linkedList = LinkedList<Int>()

                // Add initial elements to the list
                repeat(100) { linkedList.add(it) }

                // Launch coroutines for concurrent modification and iteration
                launch {
                    repeat(100) {
                        linkedList.add(it)
                        delay(10) // Adding a small delay
                    }
                }

                launch {
                    linkedList.forEach {
                        println(it)
                        delay(5) // Adding a small delay
                    }
                }

                // Wait for coroutines to finish
                delay(1000)
            }
        }
    }

    @Test
    fun testConcurrentAccess() = runTest {
        val concurrentQueue = ConcurrentLinkedQueue<Int>()

        // Add initial elements to the queue
        repeat(100) { concurrentQueue.add(it) }

        var exceptionThrown = false

        try {
            // Launch coroutines for concurrent modification and iteration
            launch {
                repeat(100) {
                    concurrentQueue.add(it)
                    delay(10) // Adding a small delay
                }
            }

            launch {
                concurrentQueue.forEach {
                    println(it) // mimics some processing
                    delay(5) // Adding a small delay
                }
            }

            // Wait for coroutines to finish
            delay(1000)
        } catch (e: ConcurrentModificationException) {
            exceptionThrown = true
        }

        // Check if no exception was thrown
        assertFalse("ConcurrentModificationException was thrown unexpectedly", exceptionThrown)
    }
}