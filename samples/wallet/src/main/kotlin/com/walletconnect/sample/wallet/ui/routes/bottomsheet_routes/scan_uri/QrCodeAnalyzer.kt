package com.walletconnect.sample.wallet.ui.routes.bottomsheet_routes.scan_uri

import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

class QrCodeAnalyzer(
    private val clearAnalyzer: () -> Unit,
    private val onQrCodeScanSuccess: (String) -> Unit,
    private val onQrCodeScanFailure: () -> Unit,
) : ImageAnalysis.Analyzer {
    private val barcodeScannerOptions = BarcodeScannerOptions.Builder()
        .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
        .build()

    private val barcodeScanner: BarcodeScanner = BarcodeScanning.getClient(barcodeScannerOptions)

    @ExperimentalGetImage
    override fun analyze(imageProxy: ImageProxy) {
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
                                onQrCodeScanSuccess(rawBarcodeValue)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        } else {
                            onQrCodeScanFailure()
                        }
                    }
                }
                .addOnFailureListener {
                    clearAnalyzer()
                    onQrCodeScanFailure()
                }
                .addOnCompleteListener { imageProxy.close() }
        }

    }
}