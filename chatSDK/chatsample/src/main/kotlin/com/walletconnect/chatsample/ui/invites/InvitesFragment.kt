package com.walletconnect.chatsample.ui.invites

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.walletconnect.chatsample.R

class InvitesFragment : Fragment() {

    companion object {
        fun newInstance() = InvitesFragment()
    }

    private lateinit var viewModel: InvitesViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_invites, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(InvitesViewModel::class.java)
    }

}