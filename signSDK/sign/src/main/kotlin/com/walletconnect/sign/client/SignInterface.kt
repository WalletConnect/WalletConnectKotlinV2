package com.walletconnect.sign.client

import com.walletconect.android_core.network.RelayConnectionInterface

interface SignInterface {
    interface WalletDelegate {
        fun onSessionProposal(sessionProposal: Sign.Model.SessionProposal)
        fun onSessionRequest(sessionRequest: Sign.Model.SessionRequest)
        fun onSessionDelete(deletedSession: Sign.Model.DeletedSession)

        //Responses
        fun onSessionSettleResponse(settleSessionResponse: Sign.Model.SettledSessionResponse)
        fun onSessionUpdateResponse(sessionUpdateResponse: Sign.Model.SessionUpdateResponse)

        //Utils
        fun onConnectionStateChange(state: Sign.Model.ConnectionState)
        fun onError(error: Sign.Model.Error)
    }

    interface DappDelegate {
        fun onSessionApproved(approvedSession: Sign.Model.ApprovedSession)
        fun onSessionRejected(rejectedSession: Sign.Model.RejectedSession)
        fun onSessionUpdate(updatedSession: Sign.Model.UpdatedSession)
        fun onSessionEvent(sessionEvent: Sign.Model.SessionEvent)
        fun onSessionExtend(session: Sign.Model.Session)
        fun onSessionDelete(deletedSession: Sign.Model.DeletedSession)

        //Responses
        fun onSessionRequestResponse(response: Sign.Model.SessionRequestResponse)

        // Utils
        fun onConnectionStateChange(state: Sign.Model.ConnectionState)
        fun onError(error: Sign.Model.Error)
    }

    fun initialize(initial: Sign.Params.Init, onError: (Sign.Model.Error) -> Unit)
    fun setWalletDelegate(delegate: WalletDelegate)
    fun setDappDelegate(delegate: DappDelegate)
    fun connect(
        connect: Sign.Params.Connect, onProposedSequence: (Sign.Model.ProposedSequence) -> Unit,
        onError: (Sign.Model.Error) -> Unit,
    )

    fun pair(pair: Sign.Params.Pair, onError: (Sign.Model.Error) -> Unit)
    fun approveSession(approve: Sign.Params.Approve, onError: (Sign.Model.Error) -> Unit)
    fun rejectSession(reject: Sign.Params.Reject, onError: (Sign.Model.Error) -> Unit)
    fun request(request: Sign.Params.Request, onError: (Sign.Model.Error) -> Unit)
    fun respond(response: Sign.Params.Response, onError: (Sign.Model.Error) -> Unit)
    fun update(update: Sign.Params.Update, onError: (Sign.Model.Error) -> Unit)
    fun extend(extend: Sign.Params.Extend, onError: (Sign.Model.Error) -> Unit)
    fun emit(emit: Sign.Params.Emit, onError: (Sign.Model.Error) -> Unit)
    fun ping(ping: Sign.Params.Ping, sessionPing: Sign.Listeners.SessionPing? = null)
    fun disconnect(disconnect: Sign.Params.Disconnect, onError: (Sign.Model.Error) -> Unit)
    fun getListOfSettledSessions(): List<Sign.Model.Session>
    fun getListOfSettledPairings(): List<Sign.Model.Pairing>
    fun getPendingRequests(topic: String): List<Sign.Model.PendingRequest>


    interface Websocket {
        val relay: RelayConnectionInterface

        fun open(onError: (String) -> Unit) {
            relay.connect { errorMessage -> onError(errorMessage) }
        }

        fun close(onError: (String) -> Unit) {
            relay.disconnect { errorMessage -> onError(errorMessage) }
        }
    }
}