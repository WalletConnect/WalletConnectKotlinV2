package com.walletconnect.dapp.ui.selected_account

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.walletconnect.dapp.R
import com.walletconnect.dapp.databinding.FragmentSelectedAccountBinding
import com.walletconnect.dapp.ui.BottomVerticalSpaceItemDecoration
import com.walletconnect.dapp.ui.NavigationEvents

class SelectedAccountFragment : Fragment() {
    private val viewModel: SelectedAccountViewModel by viewModels()
    private var _binding: FragmentSelectedAccountBinding? = null
    private val binding: FragmentSelectedAccountBinding
        get() = _binding!!
    private val selectedAccountAdapter by lazy {
        SelectedAccountAdapter() { methodName ->
            viewModel.requestMethod(methodName)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSelectedAccountBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.navigation.observe(viewLifecycleOwner) { navigationEvent ->
            when(navigationEvent) {
                is NavigationEvents.RequestSuccess -> onRequest(navigationEvent.result)
                is NavigationEvents.RequestPeerError -> onRequest(navigationEvent.errorMsg)
                is NavigationEvents.RequestError -> onRequest(navigationEvent.exceptionMsg)
                is NavigationEvents.UpgradedSelectedAccountUI -> {
                    selectedAccountAdapter.submitList(navigationEvent.selectedAccountUI.listOfMethods)
                }
                is NavigationEvents.Disconnect -> findNavController().navigate(R.id.action_fragment_selected_account_to_connect_graph)
                else -> Unit
            }
        }

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

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }

}