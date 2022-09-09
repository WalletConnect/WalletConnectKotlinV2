package com.walletconnect.responder.ui.request

import com.walletconnect.responder.ui.events.ResponderEvents

//todo: remove after implementing pending request
object RequestStore {
    var currentRequest: ResponderEvents.OnRequest? = null
}