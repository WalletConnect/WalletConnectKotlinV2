package com.walletconnect.web3.modal.ui.utils

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost

@Composable
internal fun AnimatedNavGraph(
    navController: NavHostController,
    startDestination: String,
    builder: NavGraphBuilder.() -> Unit
) {
    val stiffnessAnimSpec = spring(stiffness = Spring.StiffnessLow, visibilityThreshold = IntOffset.VisibilityThreshold)
    val tweenAnimSpec = tween<Float>(durationMillis = 400)
    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = { slideInHorizontally(animationSpec = stiffnessAnimSpec) { it / 2 } + fadeIn(animationSpec = tweenAnimSpec) },
        exitTransition = { slideOutHorizontally(animationSpec = stiffnessAnimSpec) + fadeOut(animationSpec = tweenAnimSpec) },
        popEnterTransition = { slideInHorizontally(animationSpec = stiffnessAnimSpec) + fadeIn(animationSpec = tweenAnimSpec) },
        popExitTransition = { slideOutHorizontally(animationSpec = stiffnessAnimSpec) { it / 2 } + fadeOut(animationSpec = tweenAnimSpec) },
        modifier = Modifier.animateContentSize(animationSpec = spring(stiffness = Spring.StiffnessMedium, visibilityThreshold = IntSize.VisibilityThreshold)),
        builder = builder
    )
}