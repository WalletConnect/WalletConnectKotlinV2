@file:JvmSynthetic

package com.walletconnect.walletconnectv2.network

import com.walletconnect.walletconnectv2.core.model.vo.SubscriptionIdVO
import com.walletconnect.walletconnectv2.core.model.vo.TopicVO
import com.walletconnect.walletconnectv2.network.model.RelayEvent
import com.walletconnect.walletconnectv2.network.model.RelayDTO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow

interface Relay {
    val eventsFlow: SharedFlow<RelayEvent>

    val subscriptionRequest: Flow<RelayDTO.Subscription.Request>

    fun publish(topic: TopicVO, message: String, prompt: Boolean = false, onResult: (Result<RelayDTO.Publish.Acknowledgement>) -> Unit = {})

    fun subscribe(topic: TopicVO, onResult: (Result<RelayDTO.Subscribe.Acknowledgement>) -> Unit)

    fun unsubscribe(topic: TopicVO, subscriptionId: SubscriptionIdVO, onResult: (Result<RelayDTO.Unsubscribe.Acknowledgement>) -> Unit)
}