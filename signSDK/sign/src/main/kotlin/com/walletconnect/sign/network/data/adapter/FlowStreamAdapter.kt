@file:JvmSynthetic

package com.walletconnect.sign.network.data.adapter

import com.tinder.scarlet.Stream
import com.tinder.scarlet.StreamAdapter
import com.tinder.scarlet.utils.getRawType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.reactive.collect
import java.lang.reflect.Type

internal class FlowStreamAdapter<T> : StreamAdapter<T, Flow<T>> {
    
    override fun adapt(stream: Stream<T>) = flow {
        stream.collect {
            emit(it)
        }
    }

    internal class Factory : StreamAdapter.Factory {

        override fun create(type: Type): StreamAdapter<Any, Any> {
            return when (type.getRawType()) {
                Flow::class.java -> FlowStreamAdapter()
                else -> throw IllegalArgumentException()
            }
        }
    }
}