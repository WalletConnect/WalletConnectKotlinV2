package com.walletconnect.sign.test.utils.dapp

import com.walletconnect.sign.BuildConfig
import com.walletconnect.sign.client.Sign
import com.walletconnect.sign.client.SignClient
import com.walletconnect.sign.test.utils.globalOnError
import junit.framework.TestCase.fail
import timber.log.Timber
import kotlin.time.Duration.Companion.seconds

open class DappDelegate : SignClient.DappDelegate {
    override fun onSessionRejected(rejectedSession: Sign.Model.RejectedSession) {}
    override fun onSessionUpdate(updatedSession: Sign.Model.UpdatedSession) {}
    override fun onSessionEvent(sessionEvent: Sign.Model.SessionEvent) {}
    override fun onSessionExtend(session: Sign.Model.Session) {}
    override fun onSessionDelete(deletedSession: Sign.Model.DeletedSession) {}
    override fun onSessionRequestResponse(response: Sign.Model.SessionRequestResponse) {}
    override fun onSessionApproved(approvedSession: Sign.Model.ApprovedSession) {}
    override fun onConnectionStateChange(state: Sign.Model.ConnectionState) {
        Timber.d("Dapp: onConnectionStateChange: $state")
    }

    override fun onError(error: Sign.Model.Error) {
        globalOnError(error)
    }
}

open class AutoApproveDappDelegate(val onSessionApprovedSuccess: (approvedSession: Sign.Model.ApprovedSession) -> Unit) : DappDelegate() {
    override fun onSessionApproved(approvedSession: Sign.Model.ApprovedSession) {
        approvedSession.onSessionApproved { onSessionApprovedSuccess(approvedSession) }
    }
}

fun Sign.Model.ApprovedSession.onSessionApproved(onSuccess: () -> Unit) {
    Timber.d("dappDelegate: onSessionApproved")

    DappSignClient.ping(Sign.Params.Ping(topic, BuildConfig.TEST_TIMEOUT_SECONDS.seconds), object : Sign.Listeners.SessionPing {
        override fun onSuccess(pingSuccess: Sign.Model.Ping.Success) {
            Timber.d("dappDelegate: onPingSuccess")
            onSuccess()
        }

        override fun onError(pingError: Sign.Model.Ping.Error) {
            fail(pingError.error.message)
        }
    })
}