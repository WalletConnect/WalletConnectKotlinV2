package com.walletconnect.dapp.ui.selected_account

import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.walletconnect.dapp.R
import com.walletconnect.dapp.databinding.FragmentSelectedAccountBinding
import com.walletconnect.dapp.ui.SampleDappEvents
import com.walletconnect.sample_common.BottomVerticalSpaceItemDecoration
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class SelectedAccountFragment : Fragment(R.layout.fragment_selected_account) {
    private val viewModel: SelectedAccountViewModel by viewModels()
    private var _binding: FragmentSelectedAccountBinding? = null
    private val selectedAccountAdapter by lazy {
        SelectedAccountAdapter() { methodName ->
            viewModel.requestMethod(methodName, ::sendRequestDeepLink)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentSelectedAccountBinding.bind(view).also { _binding = it }

        viewModel.navigation
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.RESUMED)
            .onEach { navigationEvent ->
                when (navigationEvent) {
                    is SampleDappEvents.RequestSuccess -> onRequest(navigationEvent.result)
                    is SampleDappEvents.RequestPeerError -> onRequest(navigationEvent.errorMsg)
                    is SampleDappEvents.RequestError -> onRequest(navigationEvent.exceptionMsg)
                    is SampleDappEvents.UpgradedSelectedAccountUI -> {
                        selectedAccountAdapter.submitList(navigationEvent.selectedAccountUI.listOfMethods)
                    }
                    is SampleDappEvents.Disconnect -> findNavController().navigate(R.id.action_fragment_selected_account_to_connect_graph)
                    else -> Unit
                }
            }.launchIn(viewLifecycleOwner.lifecycleScope)

        val selectedAccountKey = getString(R.string.selected_account)

        arguments?.takeIf {
            it.containsKey(selectedAccountKey) && it.getString(selectedAccountKey) != null
        }?.let { argumentsBundle ->
            val selectedAccountUI = viewModel.getSelectedAccount(argumentsBundle.getString(selectedAccountKey)!!)

            Glide.with(binding.root)
                .load(selectedAccountUI.icon)
                .into(binding.ivAccountIcon)

            binding.tvAccountName.text = selectedAccountUI.chainName
            binding.tvAccountAddress.text = selectedAccountUI.account

            with(binding.rvMethods) {
                addItemDecoration(BottomVerticalSpaceItemDecoration(8))
                adapter = selectedAccountAdapter.apply {
                    submitList(selectedAccountUI.listOfMethods)
                }
            }
        }
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
            // There is no app to handle deep link
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }
}