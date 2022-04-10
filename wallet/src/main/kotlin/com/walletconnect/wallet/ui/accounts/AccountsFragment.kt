package com.walletconnect.wallet.ui.accounts

import android.os.Bundle
import android.view.*
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
import com.walletconnect.wallet.ACCOUNTS_ARGUMENT_KEY
import com.walletconnect.wallet.R
import com.walletconnect.wallet.databinding.FragmentAccountsBinding
import com.walletconnect.wallet.databinding.ToolbarItemAccountsBinding
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class AccountsFragment : Fragment(R.layout.fragment_accounts) {
    private val viewModel: AccountsViewModel by navGraphViewModels(R.id.accounts_graph)
    private var _binding: FragmentAccountsBinding? = null
    private val accountAdapter by lazy { AccountAdapter() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)

        val binding = FragmentAccountsBinding.bind(view).also { _binding = it }

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

        _binding = null
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

    private fun test(): TextView {
        return ToolbarItemAccountsBinding.inflate(requireActivity().layoutInflater, _binding?.root, false).root
    }

    private fun setupAccountSelector() {

    }
}