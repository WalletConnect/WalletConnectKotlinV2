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
//            permissions = WalletConnect.Model.SessionPermissions(
//                WalletConnect.Model.Blockchain(listOf("eip155:69")),
//                WalletConnect.Model.Jsonrpc(listOf("personal_sign"))
//            ), pairingTopic = null
//        )
//        val uri = WalletConnectClient.connect(connectParams)
//
//        val qr = QRCode.from(uri).withSize(256, 256).file()
//        Glide.with(requireContext()).load(qr).into(binding.qrCode)
    }

    private fun setupToolbar() {
        binding.dappToolbar.title = getString(R.string.app_name)
        binding.dappToolbar.setOnMenuItemClickListener { false }
    }

    override fun onPairingSettled(settledPairing: WalletConnect.Model.SettledPairing) {

    }

    override fun onPairingUpdated(pairing: WalletConnect.Model.PairingUpdate) {

    }

    override fun onSessionApproved(approvedSession: WalletConnect.Model.ApprovedSession) {

    }

    override fun onSessionRejected(rejectedSession: WalletConnect.Model.RejectedSession) {

    }

    override fun onSessionUpdate(updatedSession: WalletConnect.Model.UpdatedSession) {

    }

    override fun onSessionUpgrade(upgradedSession: WalletConnect.Model.UpgradedSession) {

    }

    override fun onSessionDelete(deletedSession: WalletConnect.Model.DeletedSession) {

    }

    override fun onSessionPayloadResponse(response: WalletConnect.Model.SessionPayloadResponse) {

    }
}