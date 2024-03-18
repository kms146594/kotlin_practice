package com.example.stopwatch

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.stopwatch.databinding.ActivityMainBinding
import java.util.Timer
import kotlin.concurrent.timer

class MainActivity : AppCompatActivity() {

    val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    // 실행 상태
    var isRunning = false
    var timer : Timer? = null
    var time = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.btnRefresh.setOnClickListener {
            refresh()
        }

        binding.btnStart.setOnClickListener {
            if (isRunning) {
                pause()
            } else {
                start()
            }
        }

    }

    private fun start() {
        // 텍스트뷰 상태 변경
        binding.btnStart.text = "일시정지"
        binding.btnStart.setBackgroundColor(getColor(R.color.red))
        // 실행 상태 변경
        isRunning = true

        // 스탑워치를 시작하는 로직
        // 일정한 주기로 반복하는 동작 수행 시 유용
        // 백그라운드 스레드에서 10밀리초마다 실행
        timer = timer(period = 10) {
            time++ // 10밀리초 단위 타이머, 0.01초마다 time에 1을 더함

            // 시간계산
            val milli_second = time % 100
            val second = (time % 6000) / 100
            val minute = time / 6000

            // UI 스레드 생성 -> 텍스트뷰를 수정하기 위한 작업이 UI스레드에서 진행
            runOnUiThread {
                // UI 스레드 업데이트 조건
                if (isRunning) {
                    // 밀리초 (if문은 텍스트 길이 2자리 유지)
                    binding.tvMillisecond.text =
                        if (milli_second < 10) ".0${milli_second}"
                        else ".${milli_second}"

                    // 초 (if문은 텍스트 길이 2자리 유지)
                    binding.tvSecond.text =
                        if (second < 10) ":0${second}"
                        else ":${second}"

                    // 뷴
                    binding.tvMinute.text = "${minute}"
                }
            };
        }
    }

    private fun pause() {
        binding.btnStart.text = "시작"
        binding.btnStart.setBackgroundColor(getColor(R.color.blue))

        isRunning = false
        // 백그라운드 스레드에 있는 큐를 정리
        timer?.cancel()
    }

    // 타이머가 실행중이든, 일시정지이든 초기화되어야 함
    private fun refresh() {
        // 백그라운드 타이머 멈추기
        timer?.cancel()
        // 멈춤 상태로 변경
        binding.btnStart.text = "시작"
        binding.btnStart.setBackgroundColor(getColor(R.color.blue))
        isRunning = false

        // 타이머 초기화
        time = 0
        binding.tvMillisecond.text = ".00"
        binding.tvSecond.text = ":00"
        binding.tvMinute.text = "00"
    }

}