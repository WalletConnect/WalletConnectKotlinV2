package com.walletconnect.showcase.ui.routes.bottomsheet_routes.scan_uri

import android.graphics.ImageFormat
import android.util.Log
import android.widget.Toast
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer
import com.walletconnect.sample_common.tag
import java.nio.ByteBuffer

class QrCodeAnalyzer(
    private val clearAnalyzer: () -> Unit,
    private val onQrCodeScanSuccess: (String) -> Unit,
    private val onQrCodeScanFailure: () -> Unit,
) : ImageAnalysis.Analyzer {
    private val barcodeScannerOptions = BarcodeScannerOptions.Builder()
        .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
        .build()

    private val barcodeScanner: BarcodeScanner = BarcodeScanning.getClient(barcodeScannerOptions)

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