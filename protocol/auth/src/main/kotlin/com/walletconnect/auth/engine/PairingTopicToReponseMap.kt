package com.walletconnect.auth.engine

import com.walletconnect.foundation.common.model.Topic

// idea: If we need responseTopic persistence throughout app terminations this is not sufficient
internal val pairingTopicToResponseTopicMap: MutableMap<Topic, Topic> = mutableMapOf()