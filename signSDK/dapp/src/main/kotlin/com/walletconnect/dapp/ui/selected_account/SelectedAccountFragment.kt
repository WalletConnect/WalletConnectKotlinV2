package com.walletconnect.dapp.ui.selected_account

import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.walletconnect.dapp.R
import com.walletconnect.dapp.databinding.FragmentSelectedAccountBinding
import com.walletconnect.dapp.ui.SampleDappEvents
import com.walletconnect.sample_common.BottomVerticalSpaceItemDecoration
import com.walletconnect.sample_common.viewBinding
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class SelectedAccountFragment : Fragment(R.layout.fragment_selected_account) {
    private val binding by viewBinding(FragmentSelectedAccountBinding::bind)
    private val viewModel: SelectedAccountViewModel by viewModels()
    private val selectedAccountAdapter by lazy {
        SelectedAccountAdapter() { methodName ->
            viewModel.requestMethod(methodName, ::sendRequestDeepLink)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val selectedAccountKey = getString(R.string.selected_account)
        arguments?.takeIf {
            it.containsKey(selectedAccountKey) && it.getString(selectedAccountKey) != null
        }?.let { argumentsBundle ->
            val selectedAccount = requireNotNull(argumentsBundle.getString(selectedAccountKey))
            viewModel.fetchAccountDetails(selectedAccount)
        }

        with(binding.rvMethods) {
            addItemDecoration(BottomVerticalSpaceItemDecoration(8))
            adapter = selectedAccountAdapter
        }

        viewModel.uiState
            .flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .filterIsInstance<SelectedAccountUI.Content>()
            .onEach { uiState ->
                Glide.with(binding.root)
                    .load(uiState.icon)
                    .into(binding.ivAccountIcon)

                binding.tvAccountName.text = uiState.chainName
                binding.tvAccountAddress.text = uiState.account

                selectedAccountAdapter.submitList(uiState.listOfMethods)
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.event
            .flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .onEach { event ->
                when (event) {
                    is SampleDappEvents.RequestSuccess -> onRequest(event.result)
                    is SampleDappEvents.RequestPeerError -> onRequest(event.errorMsg)
                    is SampleDappEvents.RequestError -> onRequest(event.exceptionMsg)
                    is SampleDappEvents.Disconnect -> findNavController().navigate(R.id.action_fragment_selected_account_to_connect_graph)
                    else -> Unit
                }
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun onRequest(requestMsg: String) {
        AlertDialog.Builder(requireContext())
            .setMessage(requestMsg)
            .setNeutralButton("Ok") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun sendRequestDeepLink(sessionRequestDeeplinkUri: Uri) {
        try {
            requireActivity().startActivity(Intent(Intent.ACTION_VIEW, sessionRequestDeeplinkUri))
        } catch (exception: ActivityNotFoundException) {

            Log.e("kobe", "Dapp No Activity: $exception")
            // There is no app to handle deep link
        }
    }
}