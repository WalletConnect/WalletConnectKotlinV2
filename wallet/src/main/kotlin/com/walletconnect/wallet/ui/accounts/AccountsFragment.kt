package com.walletconnect.wallet.ui.accounts

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.walletconnect.sample_common.BottomVerticalSpaceItemDecoration
import com.walletconnect.sample_common.viewBinding
import com.walletconnect.wallet.ACCOUNTS_ARGUMENT_KEY
import com.walletconnect.wallet.R
import com.walletconnect.wallet.databinding.FragmentAccountsBinding
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class AccountsFragment : Fragment(R.layout.fragment_accounts) {
    private val binding by viewBinding(FragmentAccountsBinding::bind)
    private val viewModel: AccountsViewModel by navGraphViewModels(R.id.accounts_graph)
    private val accountAdapter by lazy { AccountAdapter() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)

        requireActivity().intent?.takeIf { intent -> intent.action == Intent.ACTION_VIEW && !intent.dataString.isNullOrBlank() }?.let { intent ->
            viewModel.pair(intent.dataString.toString()).also {
                intent.data = null
            }
        }

        (requireActivity() as AppCompatActivity).supportActionBar?.let {
            it.setDisplayShowTitleEnabled(false)
            it.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
            it.setCustomView(R.layout.toolbar_item_accounts)
        }

        with(binding.root) {
            adapter = accountAdapter
            addItemDecoration(BottomVerticalSpaceItemDecoration(24))
        }

        viewModel.accountUI
            .flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .onEach { mapOfTotalAccounts ->
                val selectedAccount = mapOfTotalAccounts.first { it.isSelected }

                accountAdapter.submitList(selectedAccount.chainAddressList)

                (requireActivity() as AppCompatActivity).supportActionBar?.customView?.findViewById<TextView>(R.id.tvAccountsSelector)?.run {
                    text = selectedAccount.title
                    setOnClickListener {
                        PopupMenu(requireContext(), this).apply {
                            mapOfTotalAccounts.onEachIndexed { index, accountUI ->
                                menu.add(1, index, 0, accountUI.title)
                            }
                            setOnMenuItemClickListener { menuItem ->
                                viewModel.newAccountClicked(menuItem.itemId)

                                false
                            }
                        }.show()
                    }
                }
            }.launchIn(viewLifecycleOwner.lifecycleScope)

        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<String>(ACCOUNTS_ARGUMENT_KEY)?.observe(viewLifecycleOwner) { pairingUri ->
            viewModel.pair(pairingUri)
            findNavController().currentBackStackEntry?.savedStateHandle?.remove<String>(ACCOUNTS_ARGUMENT_KEY)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        (requireActivity() as AppCompatActivity).supportActionBar?.let {
            it.setDisplayShowTitleEnabled(true)
            it.displayOptions = ActionBar.DISPLAY_SHOW_TITLE
            it.customView = null
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.wallet_toolbar_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.pasteUri -> {
                findNavController().navigate(R.id.action_fragment_accounts_to_dialog_paste_uri)
                false
            }
            R.id.qrCodeScanner -> {
                findNavController().navigate(R.id.action_fragment_accounts_to_fragment_scanner)
                false
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}