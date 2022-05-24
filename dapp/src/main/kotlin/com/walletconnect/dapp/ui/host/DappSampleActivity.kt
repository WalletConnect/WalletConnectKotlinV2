package com.walletconnect.dapp.ui.host

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.google.android.material.snackbar.Snackbar
import com.walletconnect.dapp.R
import com.walletconnect.dapp.domain.DappDelegate
import com.walletconnect.dapp.ui.SampleDappEvents
import com.walletconnect.sample_common.tag
import com.walletconnect.walletconnectv2.client.Sign
import com.walletconnect.walletconnectv2.client.SignClient

class DappSampleActivity : AppCompatActivity(R.layout.activity_dapp) {
    private val emittedEventsViewModel: EmittedEventsViewModel by viewModels()

    private val navHostFragment by lazy {
        supportFragmentManager.findFragmentById(R.id.fcvHost) as NavHostFragment
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        NavigationUI.setupActionBarWithNavController(this, navHostFragment.navController)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                emittedEventsViewModel.emittedEvents.collect { event ->
                    when (event) {
                        is SampleDappEvents.SessionEvent -> {
                            Log.d(tag(this), "SessionEvent")
                            (supportFragmentManager.findFragmentById(R.id.fcvHost) as NavHostFragment).view?.rootView?.let { view ->
                                Snackbar.make(view, event.toString(), Snackbar.LENGTH_LONG).show()
                            }
                        }
                        else -> Unit
                    }
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navHostFragment.navController.navigateUp() || super.onSupportNavigateUp()
    }

    override fun onDestroy() {
        super.onDestroy()

        DappDelegate.selectedSessionTopic?.let {
            val disconnectParams = Sign.Params.Disconnect(sessionTopic = it, reason = "shutdown", reasonCode = 400)
            SignClient.disconnect(disconnectParams) { error ->
                Log.e(tag(this), error.throwable.stackTraceToString())
            }
        }
    }
}

