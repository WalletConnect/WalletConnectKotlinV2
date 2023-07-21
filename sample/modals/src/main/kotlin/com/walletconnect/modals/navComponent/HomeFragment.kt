package com.walletconnect.modals.navComponent

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.walletconnect.modals.R
import com.walletconnect.modals.common.HomeViewModel
import com.walletconnect.modals.databinding.FragmentHomeBinding
import com.walletconnect.sample.common.viewBinding
import com.walletconnect.web3.modal.domain.configuration.Config
import com.walletconnect.web3.modal.ui.navigateToWeb3modal

class HomeFragment : Fragment(R.layout.fragment_home) {

    private val binding by viewBinding(FragmentHomeBinding::bind)
    private val viewModel: HomeViewModel by navGraphViewModels(R.id.nav_graph)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.connectButton.setOnClickListener {
            viewModel.connectYourWallet { uri ->
                findNavController().navigateToWeb3modal(
                    id = R.id.bottomSheet,
                    config = Config.Connect(uri)
                )
            }
        }
    }
}