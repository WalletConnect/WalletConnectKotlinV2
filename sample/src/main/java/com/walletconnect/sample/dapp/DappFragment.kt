package com.walletconnect.sample.dapp

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.walletconnect.sample.R
import com.walletconnect.sample.databinding.DappFragmentBinding
import com.walletconnect.walletconnectv2.client.WalletConnect
import com.walletconnect.walletconnectv2.client.WalletConnectClient

class DappFragment : Fragment(R.layout.dapp_fragment), WalletConnectClient.DappDelegate {

    private lateinit var binding: DappFragmentBinding

    init {
        WalletConnectClient.setDappDelegate(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DappFragmentBinding.bind(view)
        setupToolbar()

        //TODO: Improve the dApp sample app. Code below is only for test purposes!
//        val connectParams = WalletConnect.Params.Connect(
//            permissions = WalletConnect.Model.SessionPermissions(WalletConnect.Model.JsonRpc(listOf("eth_sign"))),
//            blockchain = WalletConnect.Model.Blockchain(listOf("eip155:42")),
//            pairingTopic = null)
////
//        WalletConnectClient.connect(connectParams, onWalletConnectUri = { proposedSequence ->
//            (proposedSequence as WalletConnect.Model.ProposedSequence.Pairing).apply {
//                val qr = QRCode.from(proposedSequence.uri).withSize(256, 256).file()
//                Glide.with(requireContext()).load(qr).into(binding.qrCode)
//            }
//        })
    }

    private fun setupToolbar() {
        binding.dappToolbar.title = getString(R.string.app_name)
        binding.dappToolbar.setOnMenuItemClickListener { false }
    }

    override fun onSessionApproved(approvedSession: WalletConnect.Model.ApprovedSession) {
    }

    override fun onSessionPayloadResponse(response: WalletConnect.Model.SessionPayloadResponse) {
    }

    override fun onSessionRejected(rejectedSession: WalletConnect.Model.RejectedSession) {
    }

    override fun onSessionUpdateAccounts(updatedSession: WalletConnect.Model.UpdatedSessionAccounts) {
    }

    override fun onSessionUpdateMethods(updatedSession: WalletConnect.Model.UpdatedSessionMethods) {

    }

    override fun onSessionUpdateEvents(updatedSession: WalletConnect.Model.UpdatedSessionEvents) {

    }

    override fun onSessionDelete(deletedSession: WalletConnect.Model.DeletedSession) {
    }

    override fun onUpdateSessionExpiry(session: WalletConnect.Model.Session) {

    }
}