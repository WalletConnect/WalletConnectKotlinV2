package org.walletconnect.walletconnectv2.common.network.adapters

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test
import org.walletconnect.walletconnectv2.common.Topic
import org.walletconnect.walletconnectv2.getRandom64ByteString

internal class TopicAdapterTest {
    private val moshi = Moshi.Builder()
        .add(TopicAdapter)
        .add(KotlinJsonAdapterFactory())
        .build()

    @Test
    fun toJson() {
        val topic = Topic(getRandom64ByteString())
        val expected = """"${topic.topicValue}""""

        val topicJson = moshi.adapter(Topic::class.java).toJson(topic)

        assertEquals(expected, topicJson)
    }
}