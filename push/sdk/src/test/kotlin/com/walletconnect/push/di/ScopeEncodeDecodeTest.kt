package com.walletconnect.push.di

import org.junit.Test

class ScopeEncodeDecodeTest {
    private val wellKnownPushConfig = mapOf(
        "promotional" to ("Get notified when new features or products are launched" to true),
        "transactional" to ("Get notified when new on-chain transactions target your account" to false),
        "private" to ("Get notified when new updates or offers are sent privately to your account" to true),
        "alerts" to ("Get notified when urgent action is required from your account" to false),
        "gm_hourly" to ("Get notified every hour with a gm notification" to true)
    )

    private fun decode(databaseValue: String): Map<String, Pair<String, Boolean>> {
        // Split string by | to get each entry
        return databaseValue.split("|").associate { entry ->
            // Split each entry by = to get key and value
            val entries = entry.split("=")
            entries.first().trim() to entries.last().split(",").run {
                // Split value by , to get description and isSubscribed
                this.first().trim() to this.last().trim().toBoolean()
            }
        }
    }

    private fun encode(value: Map<String, Pair<String, Boolean>>): String {
        return value.entries.joinToString(separator = "|") { entry: Map.Entry<String, Pair<String, Boolean>> ->
            "${entry.key}=${entry.value.first},${entry.value.second}"
        }
    }

    @Test
    fun testEncodeDecode() {
        val encoded = encode(wellKnownPushConfig)
        val decoded = decode(encoded)
        assert(decoded == wellKnownPushConfig)
    }
}