package com.walletconnect.sign.network.service

import com.tinder.scarlet.WebSocket
import com.tinder.scarlet.ws.Receive
import com.tinder.scarlet.ws.Send
import com.walletconnect.sign.network.model.RelayDTO
import kotlinx.coroutines.flow.Flow

internal interface RelayService {

    @Receive
    fun observeWebSocketEvent(): Flow<WebSocket.Event>

    @Send
    fun publishRequest( publishRequest: RelayDTO.Publish.Request)

    @Receive
    fun observePublishAcknowledgement(): Flow<RelayDTO.Publish.Acknowledgement>

    @Receive
    fun observePublishError(): Flow<RelayDTO.Publish.JsonRpcError>

    @Send
    fun subscribeRequest(subscribeRequest: RelayDTO.Subscribe.Request)

    @Receive
    fun observeSubscribeAcknowledgement(): Flow<RelayDTO.Subscribe.Acknowledgement>

    @Receive
    fun observeSubscribeError(): Flow<RelayDTO.Subscribe.JsonRpcError>

    @Receive
    fun observeSubscriptionRequest(): Flow<RelayDTO.Subscription.Request>

    @Send
    fun publishSubscriptionAcknowledgement(publishRequest: RelayDTO.Subscription.Acknowledgement)

    @Send
    fun unsubscribeRequest(unsubscribeRequest: RelayDTO.Unsubscribe.Request)

    @Receive
    fun observeUnsubscribeAcknowledgement(): Flow<RelayDTO.Unsubscribe.Acknowledgement>

    @Receive
    fun observeUnsubscribeError(): Flow<RelayDTO.Unsubscribe.JsonRpcError>
}
