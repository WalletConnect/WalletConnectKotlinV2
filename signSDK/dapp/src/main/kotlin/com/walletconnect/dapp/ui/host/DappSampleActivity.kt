package com.walletconnect.dapp.ui.host

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.google.android.material.snackbar.Snackbar
import com.walletconnect.dapp.R
import com.walletconnect.dapp.ui.SampleDappEvents
import com.walletconnect.sample_common.tag
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class DappSampleActivity : AppCompatActivity(R.layout.activity_dapp) {
    private val viewModel: DappViewModel by viewModels()

    private val navHostFragment by lazy {
        supportFragmentManager.findFragmentById(R.id.fcvHost) as NavHostFragment
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        NavigationUI.setupActionBarWithNavController(this, navHostFragment.navController)

        viewModel.emittedEvents
            .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
            .onEach { event ->
                when (event) {
                        is SampleDappEvents.SessionEvent -> {
                            Log.d(tag(this), "SessionEvent")
                            Snackbar.make(findViewById(android.R.id.content), event.toString(), Snackbar.LENGTH_LONG).show()
                        }
                        else -> Unit
                    }
            }.launchIn(lifecycleScope)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navHostFragment.navController.navigateUp() || super.onSupportNavigateUp()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        Log.e("kobe", "DeepLink intent: ${intent.toString()}")

//        navHostFragment.navController.handleDeepLink(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
//        viewModel.disconnect()
    }
}

