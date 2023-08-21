package com.walletconnect.sign.engine

import com.walletconnect.sign.engine.model.EngineDO
import java.util.LinkedList

internal val sessionRequestsQueue: LinkedList<EngineDO.SessionRequestEvent> = LinkedList()