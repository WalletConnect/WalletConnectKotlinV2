package com.walletconnect.sample.dapp.ui.routes.bottom_routes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage


@Composable
fun PairingSelectionRoute(
    navController: NavController
) {
    val viewModel: PairingSelectionViewModel = viewModel()
    val state by viewModel.state.collectAsState()

    PairingSelectionScreen(
        state = state,
        onPairingItemClick = {
            navController.popWithResult(PairingSelectionResult.SelectedPairing(it))
        },
        onNewPairingClick = {
            navController.popWithResult(PairingSelectionResult.NewPairing)
        }
    )
}

private fun NavController.popWithResult(result: PairingSelectionResult) {
    previousBackStackEntry?.savedStateHandle?.set(pairingSelectionResultKey, result)
    popBackStack()
}

@Composable
private fun PairingSelectionScreen(
    state: List<PairingSelectionUi>,
    onPairingItemClick: (Int) -> Unit,
    onNewPairingClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxHeight(.6f)
            .fillMaxWidth()
            .background(MaterialTheme.colors.background)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Text(
                text = "Select available paring or create another one",
                style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.SemiBold),
            )
        }
        itemsIndexed(state) { index, item ->
            PairingItem(
                item = item,
                index = index,
                onPairingItemClick = onPairingItemClick
            )
        }
        item {
            NewPairingButton(
                onNewPairingClick = onNewPairingClick,
                modifier = Modifier.padding(10.dp)
            )
        }
    }
}

@Composable
private fun PairingItem(
    item: PairingSelectionUi,
    index: Int,
    onPairingItemClick: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .border(
                width = 1.dp,
                color = MaterialTheme.colors.onBackground,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .clickable { onPairingItemClick(index) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = item.iconUrl,
            contentDescription = "icon ${item.name}",
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = item.name)
    }
}

@Composable
private fun NewPairingButton(
    onNewPairingClick: () -> Unit,
    modifier: Modifier
) {
    Button(
        colors = ButtonDefaults.buttonColors(
            backgroundColor = Color(0xFF3496ff),
            contentColor = Color.White
        ),
        modifier = modifier,
        onClick = onNewPairingClick
    ) {
        Text(text = "New Pairing")
    }
}
