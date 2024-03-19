package com.example.qrcodereader

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.example.qrcodereader.databinding.ActivityMainBinding
import com.google.common.util.concurrent.ListenableFuture
import java.security.Permission
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    // ListenableFuture형 변수 선언 -> 태스크가 끝났을 때 동작을 지정
    // Future : 병렬 프로그래밍에서 태스크 종료 여부 확인
    private lateinit var cameraProviderFuture : ListenableFuture<ProcessCameraProvider>

    // 태그 기능을 하는 코드
    // 권한 요청 후 결과를 onRequestPermissionResult에서 받을 때 필요
    private val PERMISSIONS_REQUEST_CODE = 1
    // 카메라 권한 지정
    private val PERMISSIONS_REQUIRED = arrayOf(Manifest.permission.CAMERA)

    // 이미지 분석이 실시간으로 이뤄짐 -> 여러번 호출 가능성
    // 이를 막기 위해 isDetected를 생성
    private var isDetected = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        if (!hasPermissions(this)) {
            // 카메라 권한을 요청
            requestPermissions(PERMISSIONS_REQUIRED, PERMISSIONS_REQUEST_CODE)
        } else {
            // 권한이 있다면 카메라를 시작함
            startCamera()
        }
    }

    // 미리보기와 이미지 분석 시작
    private fun startCamera() {

        // 객체의 참조값 할당
        cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        // future 태스크 종료 시 실행
        cameraProviderFuture.addListener(Runnable {
            // 카메라의 생명주기를 액티비티나 프래그먼트의 생명 주기와 바인드
            val cameraProvider = cameraProviderFuture.get()

            // 미리보기 객체
            val preview = getPreview()
            // 후면 카메라 선택
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            //
            val imageAnalysis = getImageAnalysis()

            // 미리보기 기능 선택
            cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis)

        }, ContextCompat.getMainExecutor(this))

    }

    // 미리보기 객체 반환
    private fun getPreview() : Preview {
        val preview : Preview = Preview.Builder().build() // preview 객체 생성
        // preview 객체에 surfaceProvider 설정
        // surface = 모여있는 픽셀들의 객체
        preview.setSurfaceProvider(binding.barcodePreview.getSurfaceProvider())

        return preview
    }

    // 권한 유무 확인
    // PERMISSIONS_REQURIED 배열의 원소가 모두 조건문을 만족하면 true 반환 / 아닌 경우 false 반환
    fun hasPermissions(context: Context) = PERMISSIONS_REQUIRED.all {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }

    // 권한 요청 콜백 함수
    // 액티비티 클래스에 포함된 함수 -> override 필요

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        //
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if (PackageManager.PERMISSION_GRANTED == grantResults.firstOrNull()) {
                Toast.makeText(this@MainActivity, "권한 요청이 승인되었습니다.", Toast.LENGTH_LONG).show()
                startCamera()
            } else {
                Toast.makeText(this@MainActivity, "권한 요청이 거부되었습니다.", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    // 사용자의 포커스가 Main으로 돌아오면 다시 QR코드를 인식할 수 있도록 오버라이드
    // isDetected를 false로
    override fun onResume() {
        super.onResume()
        isDetected = false
    }

    fun getImageAnalysis() : ImageAnalysis {
        val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()
        val imageAnalysis = ImageAnalysis.Builder().build()

        imageAnalysis.setAnalyzer(cameraExecutor,
            QRCodeAnalyzer(object : OnDetectListener {
                override fun onDetect(msg: String) {
                    // QR코드 인식 여부 확인
                    if (!isDetected) {
                        // 데이터가 감지됨 -> true
                        isDetected = true
                        // 다음 액티비티로 이동을 위한 인텐트 생성, 어디로 어떤 정보와
                        // onDect에서 넘어온 문자열 값
                        val intent = Intent(this@MainActivity, ResultActivity::class.java)
                        intent.putExtra("msg", msg)
                        startActivity(intent)
                    }
                    Toast.makeText(this@MainActivity, "${msg}", Toast.LENGTH_SHORT).show()
                }
        }))
        return imageAnalysis
    }
}