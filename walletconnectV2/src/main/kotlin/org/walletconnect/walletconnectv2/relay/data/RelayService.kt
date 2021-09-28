package org.walletconnect.walletconnectv2.relay.data

import com.tinder.scarlet.Stream
import com.tinder.scarlet.WebSocket
import com.tinder.scarlet.ws.Receive
import com.tinder.scarlet.ws.Send
import kotlinx.coroutines.flow.Flow
import org.walletconnect.walletconnectv2.relay.data.model.Relay

interface RelayService {

    // TODO: Only for tests, find way to extend interface or just create a copy of RelayService in tests and only have Stream
    @Receive
    fun observeEventsStream(): Stream<WebSocket.Event>

    @Receive
    fun observeEvents(): Flow<WebSocket.Event>

    @Send
    fun publishRequest(publishRequest: Relay.Publish.Request)

    @Receive
    fun observePublishResponse(): Flow<Relay.Publish.Response>

    @Send
    fun subscribeRequest(subscribeRequest: Relay.Subscribe.Request)

    @Receive
    fun observeSubscribeResponse(): Flow<Relay.Subscribe.Response>

    @Receive
    fun observeSubscriptionRequest(): Flow<Relay.Subscription.Request>

    @Send
    fun subscriptionResponse(subscriptionResponse: Relay.Subscription.Response)

    @Send
    fun unsubscribeRequest(unsubscribeRequest: Relay.Unsubscribe.Request)

    @Receive
    fun observeUnsubscribeResponse(): Flow<Relay.Unsubscribe.Response>
}