package com.example.qrcodereader

import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage

// 인터페이스 할당
// main에서 qr코드 데이터를 인식하려면 analyzer 객체를 생성할 때
// onDetectListener를 구현하여 주 생성자의 인수로 넘겨줘야 함
// analyzer에서는 이 리스너를 통하여 main과 소통
class QRCodeAnalyzer(val onDetectListener: OnDetectListener) : ImageAnalysis.Analyzer {

    // 바코드 스캐닝 객체 생성 후 할당
    private val scanner = BarcodeScanning.getClient()

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            // 이미지가 찍힐 당시 회전 각도 고려
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            // scanner.process를 통해 이미지 분석
            scanner.process(image)
                // QR코드 인식 성공 시 작업
                .addOnSuccessListener { qrCodes ->
                    // 리스너
                    for (qrCode in qrCodes) {
                        onDetectListener.onDetect(qrCode.rawValue ?: "")
                    }
                }
                // 실패 시 에러 로그 프린트
                .addOnFailureListener {
                    it.printStackTrace()
                }
                // 이미지 분석 완료 시, 이미지 프록시가 닫는 작업
                .addOnCompleteListener {
                    imageProxy.close()
                }
        }
    }

}