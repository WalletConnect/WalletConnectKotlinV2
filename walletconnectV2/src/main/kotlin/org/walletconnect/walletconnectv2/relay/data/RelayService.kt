package org.walletconnect.walletconnectv2.relay.data

import com.tinder.scarlet.Stream
import com.tinder.scarlet.WebSocket
import com.tinder.scarlet.ws.Receive
import com.tinder.scarlet.ws.Send
import kotlinx.coroutines.channels.ReceiveChannel
import org.walletconnect.walletconnectv2.relay.data.model.Relay

interface RelayService {

    @Receive
    fun observeEvents(): Stream<WebSocket.Event>

    @Send
    fun publishRequest(publishRequest: Relay.Publish.Request)

    @Receive
    fun observePublishResponse(): ReceiveChannel<Relay.Publish.Response>

    @Send
    fun subscribeRequest(subscribeRequest: Relay.Subscribe.Request)

    @Receive
    fun observeSubscribeResponse(): ReceiveChannel<Relay.Subscribe.Response>

    @Receive
    fun observeSubscriptionRequest(): ReceiveChannel<Relay.Subscription.Request>

    @Send
    fun subscriptionResponse(subscriptionResponse: Relay.Subscription.Response)

    @Send
    fun unsubscribeRequest(unsubscribeRequest: Relay.Unsubscribe.Request)

    @Receive
    fun observeUnsubscribeResponse(): ReceiveChannel<Relay.Unsubscribe.Response>
}