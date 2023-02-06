package com.walletconnect.wallet.ui.host

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.walletconnect.sample_common.viewBinding
import com.walletconnect.wallet.*
import com.walletconnect.wallet.databinding.ActivityWalletBinding
import com.walletconnect.wallet.ui.SampleWalletEvents
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlin.random.Random
import kotlin.random.nextUInt

class WalletSampleActivity : AppCompatActivity() {
    private val binding by viewBinding(ActivityWalletBinding::inflate)
    private val viewModel: WalletSampleViewModel by viewModels()
    private val navController by lazy {
        (supportFragmentManager.findFragmentById(R.id.fcvHost) as NavHostFragment).navController
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        viewModel.signEvents
            .flowWithLifecycle(lifecycle)
            .onEach { event ->
                when (event) {
                    is SampleWalletEvents.SessionProposal -> navController.navigate(R.id.action_global_to_session_proposal)
                    is SampleWalletEvents.SessionRequest -> {
                        navController.navigate(R.id.action_global_to_session_request,
                            bundleOf(SESSION_REQUEST_KEY to event.arrayOfArgs, SESSION_REQUEST_ARGS_NUM_KEY to event.numOfArgs))
                    }
                    else -> Unit
                }
            }
            .launchIn(lifecycleScope)

        viewModel.pushEvents
            .flowWithLifecycle(lifecycle)
            .onEach { event ->
                when(event) {
                    is SampleWalletEvents.PushRequest -> {
                        navController.navigate(R.id.action_global_to_push_request,
                            bundleOf(PUSH_REQUEST_KEY to event.arrayOfArgs, PUSH_REQUEST_ARGS_NUM_KEY to event.numOfArgs)
                        )
                    }
                    is SampleWalletEvents.PushMessage -> {
                        val notificationBuilder = NotificationCompat.Builder(this, "Push")
                            .setSmallIcon(R.drawable.ic_walletconnect_circle_blue)
                            .setContentText(event.title)
                            .setContentText(event.body)
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

                        NotificationManagerCompat.from(this).notify(Random.nextUInt().toInt(), notificationBuilder.build())
                    }
                    else -> Unit
                }
            }.launchIn(lifecycleScope)

        setupActionBarWithNavController(navController, AppBarConfiguration(setOf(R.id.fragment_accounts, R.id.fragment_active_sessions)))
        binding.bnvTabs.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.bnvTabs.isVisible = destination.id != R.id.fragment_scanner
        }

        createNotificationChannel()
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        navController.handleDeepLink(intent)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "PushSample"
            val descriptionText = "Sample for Push SDK"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("Push", name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}