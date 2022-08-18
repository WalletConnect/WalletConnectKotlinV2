@file:JvmSynthetic

package com.walletconnect.auth.client.mapper

import com.walletconnect.android_core.common.model.ConnectionState
import com.walletconnect.android_core.common.model.SDKError
import com.walletconnect.auth.client.Auth
import com.walletconnect.auth.engine.model.EngineDO

internal fun Auth.Model.AppMetaData.toEngineDO(): EngineDO.AppMetaData =
    EngineDO.AppMetaData(name, description, url, icons, redirect)

@JvmSynthetic
internal fun ConnectionState.toClientConnectionState(): Auth.Model.ConnectionState =
    Auth.Model.ConnectionState(isAvailable)

@JvmSynthetic
internal fun SDKError.toClientError(): Auth.Model.Error =
    Auth.Model.Error(this.exception)


