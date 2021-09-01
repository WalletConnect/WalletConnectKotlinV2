package org.walletconnect.walletconnectv2.data

import com.tinder.scarlet.Stream
import com.tinder.scarlet.WebSocket
import com.tinder.scarlet.ws.Receive
import com.tinder.scarlet.ws.Send

interface RelayService {

        @Receive
        fun observeEvents(): Stream<WebSocket.Event>

        @Send
        fun send(message: ByteArray)

}