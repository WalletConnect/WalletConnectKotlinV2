package org.walletconnect.walletconnectv2.relay.data

import com.tinder.scarlet.Stream
import com.tinder.scarlet.WebSocket
import com.tinder.scarlet.ws.Receive
import com.tinder.scarlet.ws.Send
import kotlinx.coroutines.flow.Flow
import org.walletconnect.walletconnectv2.relay.data.model.Relay

interface RelayService {

    @Receive
    fun observeEvents(): Stream<WebSocket.Event>

    @Send
    fun publishRequest(publishRequest: Relay.Publish.Request)

    @Receive
    fun observePublishAcknowledgement(): Flow<Relay.Publish.Acknowledgement>

    @Send
    fun subscribeRequest(subscribeRequest: Relay.Subscribe.Request)

    @Receive
    fun observeSubscribeAcknowledgement(): Flow<Relay.Subscribe.Acknowledgement>

    @Receive
    fun observeSubscriptionRequest(): Flow<Relay.Subscription.Request>

    @Send
    fun publishSubscriptionAcknowledgment(publishRequest: Relay.Subscription.Acknowledgement)

    @Send
    fun subscriptionAcknowledgement(subscriptionAcknowledgement: Relay.Subscription.Acknowledgement)

    @Send
    fun unsubscribeRequest(unsubscribeRequest: Relay.Unsubscribe.Request)

    @Receive
    fun observeUnsubscribeAcknowledgement(): Flow<Relay.Unsubscribe.Acknowledgement>
}