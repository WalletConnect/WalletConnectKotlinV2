package com.walletconnect.modals.kotlindsl

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.walletconnect.modals.R
import com.walletconnect.modals.common.HomeViewModel
import com.walletconnect.modals.databinding.FragmentHomeBinding
import com.walletconnect.sample.common.viewBinding
import com.walletconnect.web3.modal.domain.configuration.Config
import com.walletconnect.web3.modal.ui.navigateToWeb3Modal

class HomeFragment : Fragment(R.layout.fragment_home) {

    private val binding by viewBinding(FragmentHomeBinding::bind)
    private val viewModel: HomeViewModel by viewModels { defaultViewModelProviderFactory }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.connectButton.setOnClickListener {
            viewModel.connectYourWallet { uri ->
                findNavController().navigateToWeb3Modal(Config.Connect(uri))
            }
        }
    }
}