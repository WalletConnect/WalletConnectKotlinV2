@file:OptIn(ExperimentalMaterialNavigationApi::class, ExperimentalMaterialApi::class, ExperimentalMaterialNavigationApi::class)

package com.walletconnect.web3.wallet.ui.routes.bottomsheet_routes.scan_uri

import android.Manifest
import android.content.pm.PackageManager
import android.util.Size
import android.view.Surface.ROTATION_90
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.camera.view.PreviewView.StreamState
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.google.accompanist.navigation.material.BottomSheetNavigatorSheetState
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.walletconnect.web3.wallet.sample.R
import com.walletconnect.web3.wallet.ui.routes.showSnackbar
import com.walletconnect.web3.wallet.sample.closebutton.CloseButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Composable
fun ScanUriRoute(navController: NavController, sheetState: BottomSheetNavigatorSheetState, onScanSuccess: (String) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember {
        ProcessCameraProvider.getInstance(context)
    }
    var hasCamPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCamPermission = granted
        }
    )
    LaunchedEffect(key1 = true) {
        launcher.launch(Manifest.permission.CAMERA)
    }

    val previewView = remember {
        PreviewView(context)
    }

    var previewViewStreamState: StreamState? by remember {
        mutableStateOf(StreamState.IDLE)
    }

    LaunchedEffect(key1 = sheetState.currentValue) {
        if (sheetState.targetValue == ModalBottomSheetValue.Expanded && sheetState.currentValue == ModalBottomSheetValue.Expanded) {
            val preview = Preview.Builder().build()
            val selector = CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build()
            preview.setSurfaceProvider(previewView.surfaceProvider)

            val imageAnalysis = ImageAnalysis.Builder()
                .setTargetRotation(ROTATION_90)
                .setTargetResolution(Size(previewView.width, previewView.height))
                .setBackpressureStrategy(STRATEGY_KEEP_ONLY_LATEST)
                .build()

            imageAnalysis.setAnalyzer(
                ContextCompat.getMainExecutor(context),
                QrCodeAnalyzer(clearAnalyzer = imageAnalysis::clearAnalyzer, onQrCodeScanSuccess = { result ->
                    navController.popBackStack()
                    onScanSuccess(result)
                }, onQrCodeScanFailure = {
                    //todo Add Snackbar with error as a route parameter callback: https://stackoverflow.com/questions/68909340/how-to-show-snackbar-with-a-button-onclick-in-jetpack-compose
                    navController.popBackStack()
                    navController.showSnackbar("QR Scan failed")
                })
            )
            try {
                cameraProviderFuture.get().bindToLifecycle(lifecycleOwner, selector, preview, imageAnalysis)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            launch(Dispatchers.IO) {
                while (previewViewStreamState != StreamState.STREAMING) {
                    previewViewStreamState = previewView.previewStreamState.value
                    delay(100)
                }
            }
        }
    }
    ScanView(navController, hasCamPermission, previewView, previewViewStreamState)
}

@Composable
fun ScanView(navController: NavController, hasCamPermission: Boolean, previewView: PreviewView, previewViewStreamState: StreamState?) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.99f),
    ) {
        if (hasCamPermission) {
            Box {
                AndroidView(factory = { context -> previewView }, modifier = Modifier.fillMaxSize())

                if (previewViewStreamState == StreamState.IDLE) {
                    CircularProgressIndicator(
                        strokeWidth = 8.dp,
                        modifier = Modifier
                            .size(100.dp)
                            .align(Alignment.Center), color = Color(0xFFB8F53D)
                    )
                }

                val qrFocusVector = ImageVector.vectorResource(id = R.drawable.qr_focus)
                val qrFocusPainter = rememberVectorPainter(image = qrFocusVector)

                val qrBackVector = ImageVector.vectorResource(id = R.drawable.qr_back)
                val qrBackPainter = rememberVectorPainter(image = qrBackVector)

                Image(painter = qrBackPainter, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.FillWidth)
                Image(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(15.dp),
                    painter = qrFocusPainter, contentDescription = null
                )
                Text(
                    text = "Scan the code", style = TextStyle(
                        color = Color(0xffffffff), textAlign = TextAlign.Center, fontSize = 20.sp, fontWeight = FontWeight.SemiBold
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                )
                CloseButton(modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 20.dp, end = 20.dp)
                    .size(30.dp)
                    .clickable { navController.popBackStack() }
                )
            }
        }
    }
}
