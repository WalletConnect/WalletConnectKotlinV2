package com.walletconnect.wallet.ui.sessions.details

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import com.bumptech.glide.Glide
import com.walletconnect.sample_common.BottomVerticalSpaceItemDecoration
import com.walletconnect.sample_common.viewBinding
import com.walletconnect.wallet.R
import com.walletconnect.wallet.SELECTED_SESSION_TOPIC_KEY
import com.walletconnect.wallet.databinding.FragmentSessionDetailsBinding
import com.walletconnect.wallet.ui.SampleWalletEvents
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class SessionDetailsFragment : Fragment(R.layout.fragment_session_details) {
    private val binding: FragmentSessionDetailsBinding by viewBinding(FragmentSessionDetailsBinding::bind)
    private val viewModel: SessionDetailsViewModel by viewModels()
    private val chainAccountsAdapter by lazy { SessionDetailsAdapter(viewModel::updateAccounts) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)

        arguments.takeIf { it?.isEmpty == false && it.containsKey(SELECTED_SESSION_TOPIC_KEY) }?.let { args ->
            val sessionTopic: String = requireNotNull(args.getString(SELECTED_SESSION_TOPIC_KEY))
            viewModel.getSessionDetails(sessionTopic)
        } ?: Toast.makeText(requireContext(), "Unable to find selected Session", Toast.LENGTH_SHORT).show()

        with(binding.rvAccounts) {
            itemAnimator = null
            addItemDecoration(BottomVerticalSpaceItemDecoration(12))
            addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL).apply {
                val dividerDrawable = AppCompatResources.getDrawable(requireContext(), R.drawable.drawable_divider)!!
                setDrawable(dividerDrawable)
            })
            adapter = chainAccountsAdapter
        }

        viewModel.uiState
            .flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .filterNotNull()
            .onEach { sessionDetailsUI: SessionDetailsUI ->
                when (sessionDetailsUI) {
                    is SessionDetailsUI.NoContent -> findNavController().popBackStack()
                    is SessionDetailsUI.Content -> {
                        Glide.with(view)
                            .load(sessionDetailsUI.icon)
                            .into(binding.ivPeerIcon)

                        binding.tvPeerName.text = sessionDetailsUI.name
                        binding.tvPeerUrl.text = sessionDetailsUI.url
                        binding.tvPeerDescription.text = sessionDetailsUI.description
                        binding.tvMethods.text = sessionDetailsUI.methods
                        binding.btnDelete.setOnClickListener {
                            viewModel.deleteSession()
                        }

                        chainAccountsAdapter.submitList(sessionDetailsUI.listOfChainAccountInfo)
                    }
                }
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.sessionDetails
            .flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .onEach { event ->
                when (event) {
                    is SampleWalletEvents.PingSuccess -> Toast.makeText(requireContext(), "Pinged Peer Successfully on Topic: ${event.topic}", Toast.LENGTH_SHORT).show()
                    is SampleWalletEvents.PingError -> Toast.makeText(requireContext(), "Pinged Peer Unsuccessfully", Toast.LENGTH_SHORT).show()
                    is SampleWalletEvents.Disconnect -> findNavController().popBackStack()
                    else -> Unit
                }
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.session_details_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.ping -> {
                viewModel.ping()
                false
            }
            R.id.upgrade -> {
                viewModel.upgrade()
                false
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}