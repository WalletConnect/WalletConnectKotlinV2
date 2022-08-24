@file:JvmSynthetic

package com.walletconnect.auth.client.mapper

import com.walletconnect.auth.client.Auth
import com.walletconnect.auth.engine.model.EngineDO

internal fun Auth.Model.AppMetaData.toEngineDO(): EngineDO.AppMetaData =
    EngineDO.AppMetaData(name, description, url, icons, redirect)

