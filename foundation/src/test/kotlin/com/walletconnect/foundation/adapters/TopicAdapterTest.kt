package com.walletconnect.foundation.adapters

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.walletconnect.foundation.common.adapters.TopicAdapter
import com.walletconnect.foundation.common.model.Topic
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
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
        val topic = Topic("random")
        val expected = "\"${topic.value}\""

        val topicJson = moshi.adapter(Topic::class.java).toJson(topic)

        assertEquals(expected, topicJson)
    }
}