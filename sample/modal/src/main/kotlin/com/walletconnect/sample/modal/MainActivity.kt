@file:OptIn(ExperimentalMaterialNavigationApi::class, ExperimentalMaterialApi::class)

package com.walletconnect.sample.modal

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.primarySurface
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.navigation.material.BottomSheetNavigator
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.navigation.material.ModalBottomSheetLayout
import com.walletconnect.sample.modal.common.Route
import com.walletconnect.sample.modal.compose.ComposeActivity
import com.walletconnect.sample.modal.kotlindsl.KotlinDSLActivity
import com.walletconnect.sample.modal.navComponent.NavComponentActivity
import com.walletconnect.sample.modal.ui.LabScreen
import com.walletconnect.sample.modal.ui.predefinedGreenDarkTheme
import com.walletconnect.sample.modal.ui.predefinedGreenLightTheme
import com.walletconnect.sample.modal.ui.predefinedOrangeDarkTheme
import com.walletconnect.sample.modal.ui.predefinedOrangeLightTheme
import com.walletconnect.sample.modal.ui.predefinedRedDarkTheme
import com.walletconnect.sample.modal.ui.predefinedRedLightTheme
import com.walletconnect.sample.modal.ui.theme.WalletConnectTheme
import com.walletconnect.web3.modal.ui.Web3ModalTheme
import com.walletconnect.web3.modal.ui.web3ModalGraph

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WalletConnectTheme {
                val scaffoldState: ScaffoldState = rememberScaffoldState()
                val sheetState = rememberModalBottomSheetState(
                    initialValue = ModalBottomSheetValue.Hidden,
                    skipHalfExpanded = true
                )
                val bottomSheetNavigator = BottomSheetNavigator(sheetState)
                val navController = rememberNavController(bottomSheetNavigator)

                val isDarkTheme = isSystemInDarkTheme()
                var isDark by remember { mutableStateOf(isDarkTheme) }
                var darkColors by remember { mutableStateOf(Web3ModalTheme.provideDarkWeb3ModalColor()) }
                var lightColors by remember { mutableStateOf(Web3ModalTheme.provideLightWeb3ModalColors()) }

                Scaffold(
                    scaffoldState = scaffoldState,
                    topBar = {
                        Column {
                            TopAppBar(
                                title = {
                                    Text(
                                        text = "Web3Modal Lab",
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.Center
                                    )
                                },
                                navigationIcon = {},
                                actions = {
                                    ThemeModeIcon(isDark) { isDark = it }
                                }
                            )
                            PredefineThemes(
                                onClick = { light, dark ->
                                    lightColors = light
                                    darkColors = dark
                                }
                            )
                        }
                    },
                    drawerContent = { DrawerContent() }
                ) { paddingValues ->
                    Web3ModalTheme(
                        mode = if (isDark) Web3ModalTheme.Mode.DARK else Web3ModalTheme.Mode.LIGHT,
                        lightColors = lightColors,
                        darkColors = darkColors
                    ) {
                        ModalBottomSheetLayout(
                            sheetShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                            bottomSheetNavigator = bottomSheetNavigator,
                            modifier = Modifier.padding(paddingValues)
                        ) {
                            NavHost(
                                navController = navController,
                                startDestination = Route.Lab.path
                            ) {
                                composable(Route.Lab.path) {
                                    LabScreen(navController = navController)
                                }
                                web3ModalGraph(navController)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PredefineThemes(
    onClick: (Web3ModalTheme.Colors, Web3ModalTheme.Colors) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colors.primarySurface)
            .padding(horizontal = 18.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        item { ThemeItem(color = Color(0xFF47A1FF), text = "Default") { onClick(Web3ModalTheme.provideLightWeb3ModalColors(), Web3ModalTheme.provideDarkWeb3ModalColor()) } }
        item { ThemeItem(color = Color(0xFFFFA500), text = "Orange") { onClick(predefinedOrangeLightTheme, predefinedOrangeDarkTheme) } }
        item { ThemeItem(color = Color(0xFFB7342B), text = "Red") { onClick(predefinedRedLightTheme, predefinedRedDarkTheme) } }
        item { ThemeItem(color = Color(0xFF10B124), text = "Green") { onClick(predefinedGreenLightTheme, predefinedGreenDarkTheme) } }
    }
}



@Composable
private fun ThemeItem(
    color: Color,
    text: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(vertical = 4.dp, horizontal = 12.dp)
            .clickable { },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(25.dp)
                .background(color, shape = CircleShape)
                .border(width = 1.dp, color = MaterialTheme.colors.onBackground, shape = CircleShape)
                .clickable { onClick() })
        Text(text = text, style = TextStyle(fontSize = 12.sp, color = MaterialTheme.colors.onBackground))
    }
}

@Composable
private fun ThemeModeIcon(
    isDarkTheme: Boolean,
    onClick: (Boolean) -> Unit
) {
    val res = if (isDarkTheme) R.drawable.ic_day_mode else R.drawable.ic_night_mode
    Icon(
        painter = painterResource(id = res),
        contentDescription = null,
        modifier = Modifier
            .padding(12.dp)
            .size(20.dp)
            .clickable(
                indication = rememberRipple(bounded = false),
                interactionSource = remember { MutableInteractionSource() },
            ) { onClick(!isDarkTheme) }
    )
}

@Composable
private fun DrawerContent() {
    val context = LocalContext.current
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
    ) {
        Button(onClick = {
            val intent = Intent(context, ComposeActivity::class.java)
            context.startActivity(intent)
        }) {
            Text(text = "Jetpack Compose")
        }
        Spacer(modifier = Modifier.height(40.dp))
        Button(onClick = {
            val intent = Intent(context, KotlinDSLActivity::class.java)
            context.startActivity(intent)
        }) {
            Text(text = "Kotlin DSL")
        }
        Spacer(modifier = Modifier.height(40.dp))
        Button(onClick = {
            val intent = Intent(context, NavComponentActivity::class.java)
            context.startActivity(intent)
        }) {
            Text(text = "Navigation component")
        }
    }
}
