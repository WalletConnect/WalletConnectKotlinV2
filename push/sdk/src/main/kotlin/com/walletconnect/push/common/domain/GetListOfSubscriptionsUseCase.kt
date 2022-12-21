package com.walletconnect.push.common.domain

import com.walletconnect.push.common.model.EngineDO

class GetListOfSubscriptionsUseCase() {

    operator fun invoke(): Map<String, EngineDO.PushSubscription.Responded> {
        return emptyMap()
    }
}