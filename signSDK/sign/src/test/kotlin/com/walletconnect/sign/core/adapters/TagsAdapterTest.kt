package com.walletconnect.sign.core.adapters

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.walletconnect.sign.core.model.type.Tags
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class TagsAdapterTest {
    private val moshi = Moshi.Builder()
        .add { _, _, _ ->
            TagsAdapter
        }
        .add(KotlinJsonAdapterFactory())
        .build()

    @Test
    fun toJson() {
        val tag = Tags.SIGN
        val expected = """"${tag.id}""""

        val tagJson = moshi.adapter(Tags::class.java).toJson(tag)

        Assertions.assertEquals(expected, """"$tagJson"""")
    }
}