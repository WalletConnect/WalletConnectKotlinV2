package com.walletconnect.web3.modal.ui.utils

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost

@Composable
internal fun AnimatedNavGraph(
    navController: NavHostController,
    startDestination: String,
    builder: NavGraphBuilder.() -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = { slideInHorizontally { it / 2 } + fadeIn() },
        exitTransition = { slideOutHorizontally() + fadeOut() },
        popEnterTransition = { slideInHorizontally() + fadeIn() },
        popExitTransition = { slideOutHorizontally { it / 2 } + fadeOut() },
        builder = builder
    )
}