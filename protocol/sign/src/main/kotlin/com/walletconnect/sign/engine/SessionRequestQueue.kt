package com.walletconnect.sign.engine

import com.walletconnect.sign.engine.model.EngineDO
import java.util.concurrent.ConcurrentLinkedQueue

internal val sessionRequestEventsQueue = ConcurrentLinkedQueue<EngineDO.SessionRequestEvent>()