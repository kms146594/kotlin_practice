package com.example.qrcodereader

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.camera.core.Preview
import com.example.qrcodereader.databinding.ActivityResultBinding

class ResultActivity : AppCompatActivity() {

    val binding by lazy { ActivityResultBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val result = intent.getStringExtra("msg") ?: "데이터가 존재하지 않습니다."
        // UI 초기화
        setUI(result)
    }

    // 화면을 초기화하는 작업
    private fun setUI(result: String) {
        // 넘어온 QR 코드 속 데이터를 텍스트뷰에 설정
        binding.tvResult.text = result
        binding.btnGoback.setOnClickListener {
            // 돌아가기 버튼 클릭 시, 액티비티 종료
            finish()
        }
    }


}