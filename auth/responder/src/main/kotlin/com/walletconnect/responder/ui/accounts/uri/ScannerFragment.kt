package com.walletconnect.responder.ui.accounts.uri

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.mlkit.vision.barcode.Barcode
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.walletconnect.responder.R
import com.walletconnect.responder.common.ACCOUNTS_ARGUMENT_KEY
import com.walletconnect.responder.databinding.FragmentScannerBinding
import com.walletconnect.sample_common.tag
import com.walletconnect.sample_common.viewBinding
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.util.concurrent.Executors

@ExperimentalCoroutinesApi
class ScannerFragment : Fragment(R.layout.fragment_scanner) {
    private val binding: FragmentScannerBinding by viewBinding(FragmentScannerBinding::bind)
    private val cameraPermissionCallback: ActivityResultLauncher<String> = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            getProcessCameraProvider()
        }
    }

    private val cameraSelector: CameraSelector by lazy { CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (requireActivity() as AppCompatActivity).supportActionBar?.hide()

        if (isCameraPermissionGranted()) {
            getProcessCameraProvider()
        } else {
            cameraPermissionCallback.launch(Manifest.permission.CAMERA)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        (requireActivity() as AppCompatActivity).supportActionBar?.show()
    }

    private fun getProcessCameraProvider() {
        ProcessCameraProvider.getInstance(requireContext()).run {
            addListener(
                {
                    bindCameraUseCases(get())
                },
                ContextCompat.getMainExecutor(requireContext())
            )
        }
    }

    private fun bindCameraUseCases(cameraProvider: ProcessCameraProvider) {
        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(viewLifecycleOwner, cameraSelector, buildPreviewUseCase(), buildImageAnalysisUseCase())
    }

    private fun buildPreviewUseCase(): Preview {
        return Preview.Builder()
            .setTargetRotation(binding.previewView.display.rotation)
            .build().apply {
                setSurfaceProvider(binding.previewView.surfaceProvider)
            }
    }

    private fun buildImageAnalysisUseCase(): ImageAnalysis {
        val barcodeScannerOptions = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()
        val barcodeScanner: BarcodeScanner = BarcodeScanning.getClient(barcodeScannerOptions)

        return ImageAnalysis.Builder()
            .setTargetRotation(binding.previewView.display.rotation)
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build().apply {
                setAnalyzer(Executors.newSingleThreadExecutor()) { imageProxy ->
                    processImageProxy(barcodeScanner, imageProxy, this::clearAnalyzer)
                }
            }
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun processImageProxy(barcodeScanner: BarcodeScanner, imageProxy: ImageProxy, clearAnalyzer: () -> Unit) {
        val inputImage = imageProxy.image?.let {
            InputImage.fromMediaImage(it, imageProxy.imageInfo.rotationDegrees)
        } ?: return

        inputImage.let { image ->
            barcodeScanner.process(image)
                .addOnSuccessListener { barcodes: MutableList<Barcode> ->
                    if (barcodes.isNotEmpty()) {
                        val rawBarcodeValue = barcodes.first().rawValue

                        if (rawBarcodeValue != null) {
                            try {
                                imageProxy.close()
                                clearAnalyzer()

                                findNavController().previousBackStackEntry?.savedStateHandle?.set(ACCOUNTS_ARGUMENT_KEY, rawBarcodeValue)
                                findNavController().popBackStack()
                            } catch (e: Exception) {
                                Log.e(tag(this@ScannerFragment), e.stackTraceToString())
                            }
                        } else {
                            Toast.makeText(requireContext(), "Failed to find barcode", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                .addOnFailureListener {
                    Log.e(tag(this@ScannerFragment), it.message.toString())
                    Toast.makeText(requireContext(), "Failed to capture QR Code", Toast.LENGTH_SHORT).show()

                    clearAnalyzer()
                    findNavController().popBackStack()
                }
                .addOnCompleteListener { imageProxy.close() }
        }
    }

    private fun isCameraPermissionGranted(): Boolean = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
}