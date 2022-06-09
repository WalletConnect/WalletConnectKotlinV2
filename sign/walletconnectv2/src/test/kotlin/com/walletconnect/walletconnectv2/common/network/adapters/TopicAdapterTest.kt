package com.walletconnect.walletconnectv2.common.network.adapters

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.walletconnect.walletconnectv2.core.adapters.TopicAdapter
import com.walletconnect.walletconnectv2.core.model.vo.TopicVO
import com.walletconnect.walletconnectv2.util.getRandom64ByteHexString
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
        val expected = TopicVO(expectedTopicValue)

        val resultTopic = moshi.adapter(TopicVO::class.java).fromJson(expectedTopicValue)

        assertNotNull(resultTopic)
        kotlin.test.assertEquals(expected, resultTopic)
    }

    @Test
    fun toJson() {
        val topic = TopicVO(getRandom64ByteHexString())
        val expected = "\"${topic.value}\""

        val topicJson = moshi.adapter(TopicVO::class.java).toJson(topic)

        assertEquals(expected, topicJson)
    }
}