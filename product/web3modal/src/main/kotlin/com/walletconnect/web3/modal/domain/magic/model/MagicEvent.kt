package com.walletconnect.web3.modal.domain.magic.model

sealed interface MagicResult {
    val isSuccess: Boolean
    interface Success: MagicResult {
        override val isSuccess: Boolean
            get() = true
    }
    interface Error: MagicResult {
        override val isSuccess: Boolean
            get() = false
    }
}

internal sealed class MagicEvent(val type: MagicTypeResponse): MagicResult {
    internal object SyncDappDataSuccess : MagicEvent(MagicTypeResponse.SYNC_DAPP_DATA_SUCCESS), MagicResult.Success
    internal object SyncThemeSuccess : MagicEvent(MagicTypeResponse.SYNC_THEME_SUCCESS), MagicResult.Success
    internal sealed class ConnectEmailResult(type: MagicTypeResponse) : MagicEvent(type) {
        internal data class ConnectEmailSuccess(val action: String) : ConnectEmailResult(MagicTypeResponse.CONNECT_EMAIL_SUCCESS), MagicResult.Success
        internal object ConnectEmailError : ConnectEmailResult(MagicTypeResponse.CONNECT_EMAIL_ERROR), MagicResult.Error
    }

    internal sealed class IsConnectResult(type: MagicTypeResponse) : MagicEvent(type) {
        internal data class IsConnectedSuccess(val isConnected: Boolean) : IsConnectResult(MagicTypeResponse.IS_CONNECTED_SUCCESS), MagicResult.Success
        internal object IsConnectedError : IsConnectResult(MagicTypeResponse.IS_CONNECTED_ERROR), MagicResult.Error
    }

    internal sealed class ConnectOTPResult(type: MagicTypeResponse) : MagicEvent(type) {
        internal object ConnectOTPSuccess : ConnectOTPResult(MagicTypeResponse.CONNECT_OTP_SUCCESS), MagicResult.Success
        internal object ConnectOTPError : ConnectOTPResult(MagicTypeResponse.CONNECT_OTP_ERROR), MagicResult.Error
    }

    internal sealed class GetUserResult(type: MagicTypeResponse) : MagicEvent(type) {
        internal data class GetUserSuccess(
            val email: String,
            val address: String,
            val chainId: Int
        ) : GetUserResult(MagicTypeResponse.GET_USER_SUCCESS), MagicResult.Success

        internal object GetUserError : GetUserResult(MagicTypeResponse.GET_USER_ERROR), MagicResult.Error
    }

    internal data class SessionUpdate(val token: String) : MagicEvent(MagicTypeResponse.SESSION_UPDATE), MagicResult.Success
    internal sealed class SwitchNetworkResult(type: MagicTypeResponse) : MagicEvent(type) {
        internal data class SwitchNetworkSuccess(val chainId: Int) : SwitchNetworkResult(MagicTypeResponse.SWITCH_NETWORK_SUCCESS), MagicResult.Success
        internal object SwitchNetworkError : SwitchNetworkResult(MagicTypeResponse.SWITCH_NETWORK_ERROR), MagicResult.Error
    }

    internal sealed class RpcRequestResult(type: MagicTypeResponse) : MagicEvent(type) {
        internal data class RpcRequestSuccess(val hash: String) : MagicEvent(MagicTypeResponse.RPC_REQUEST_SUCCESS), MagicResult.Success
        internal data class RpcRequestError(val error: String) : MagicEvent(MagicTypeResponse.RPC_REQUEST_ERROR), MagicResult.Error
    }

    internal object SignOutSuccess : MagicEvent(MagicTypeResponse.SIGN_OUT_SUCCESS), MagicResult.Success
}
