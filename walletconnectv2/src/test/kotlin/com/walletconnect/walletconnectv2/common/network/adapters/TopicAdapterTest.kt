package com.walletconnect.walletconnectv2.common.network.adapters

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import com.walletconnect.walletconnectv2.common.Topic
import com.walletconnect.walletconnectv2.util.getRandom64ByteHexString
import kotlin.test.assertNotNull

internal class TopicAdapterTest {
    private val moshi = Moshi.Builder()
        .add { _, _, _ ->
            TopicAdapter
        }
        .add(KotlinJsonAdapterFactory())
        .build()

    @Test
    fun fromJson() {
        val expectedTopicValue = "AABBCCDDEEFFGGHHIIJJKKLLMMNNOOPPQQRRSSTTUUVVWWXXYYZZAABBCCDDEEFF"
        val expected = Topic(expectedTopicValue)

        val resultTopic = moshi.adapter(Topic::class.java).fromJson(expectedTopicValue)

        assertNotNull(resultTopic)
        kotlin.test.assertEquals(expected, resultTopic)
    }

    @Test
    fun toJson() {
        val topic = Topic(getRandom64ByteHexString())
        val expected = "\"${topic.value}\""

        val topicJson = moshi.adapter(Topic::class.java).toJson(topic)

        assertEquals(expected, topicJson)
    }
}