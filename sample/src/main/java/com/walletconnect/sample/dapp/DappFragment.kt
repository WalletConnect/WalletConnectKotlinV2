package com.walletconnect.sample.dapp

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.walletconnect.sample.R
import com.walletconnect.sample.databinding.DappFragmentBinding
import com.walletconnect.walletconnectv2.client.WalletConnect
import com.walletconnect.walletconnectv2.client.WalletConnectClient
import net.glxn.qrgen.android.QRCode

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
        val connectParams = WalletConnect.Params.Connect(
            permissions = WalletConnect.Model.SessionPermissions(
                WalletConnect.Model.Blockchain(listOf("eip155:69")),
                WalletConnect.Model.Jsonrpc(listOf("personal_sign"))
            ), pairingTopic = null
        )
        val uri = WalletConnectClient.connect(connectParams)

        Log.d("kobe", "Uri: $uri")

        val qr = QRCode.from(uri).withSize(256, 256).file()
        Glide.with(requireContext()).load(qr).into(binding.qrCode)
    }

    private fun setupToolbar() {
        binding.dappToolbar.title = getString(R.string.app_name)
        binding.dappToolbar.setOnMenuItemClickListener { false }
    }

    override fun onPairingSettled(settledPairing: WalletConnect.Model.SettledPairing) {
        Log.d("kobe", "settledPairing: $settledPairing")
    }

    override fun onSessionApproved(approvedSession: WalletConnect.Model.ApprovedSession) {
        Log.d("kobe", "approvedSession: $approvedSession")
    }

    override fun onSessionRejected(rejectedSession: WalletConnect.Model.RejectedSession) {
        Log.d("kobe", "rejectedSession: $rejectedSession")
    }

    override fun onSessionUpdate(updatedSession: WalletConnect.Model.UpdatedSession) {
        Log.d("kobe", "updatedSession: $updatedSession")
    }

    override fun onSessionUpgrade(upgradedSession: WalletConnect.Model.UpgradedSession) {
        Log.d("kobe", "upgradedSession: $upgradedSession")
    }

    override fun onSessionDelete(deletedSession: WalletConnect.Model.DeletedSession) {
        Log.d("kobe", "deletedSession: $deletedSession")
    }

    override fun onSessionPayloadResponse(sessionResponse: WalletConnect.Model.SessionPayloadResponse) {
        Log.d("kobe", "sessionResponse: $sessionResponse")
    }

    override fun onPairingUpdated(pairing: WalletConnect.Model.SettledPairing) {
        Log.d("kobe", "pairing: $pairing")
    }
}