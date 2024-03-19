package com.example.musicplayer

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.widget.Toast
import androidx.annotation.RequiresApi

class MusicPlayerService : Service() {

    var mMediaPlayer : MediaPlayer? = null // 미디어 플레이어 객체 null 초기화

    // 바인더를 반환해 서비스 함수를 사용할 수 있게 해줌
    var mBinder : MusicPlayerBinder = MusicPlayerBinder()

    // binder 클래스가 Ibinder 인터페이스를 구현 -> onbinde()에서 MusicPlayerBinder 클래스 객체 반환 가능
    // getService에서 연결하려는 액티비티에 현재 서비스를 반환 -> 연결된 구성 요소가 서비스의 함수 사용 가능
    inner class MusicPlayerBinder : Binder() {
        fun getService(): MusicPlayerService {
            return this@MusicPlayerService
        }
    }

    // 서비스가 생성될 때 단 한번만 실행
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()
        // 포그라운드 서비스 시작
        // 상태표시줄에 서비스 실행 알림 생성
        startForegroundService()
    }

    // 바인드
    // 액티비티와 같은 구성요소에서 bindService()함수 호출 시 실행
    // -> 서비스와 구성요소의 연결 매개체 역할을 하는 IBinder 반환
    // 바인드가 필요없는 서비스(ex 시작된 서비스) -> null 반환
    override fun onBind(intent: Intent?): IBinder {
        return mBinder
    }

    // 시작된 상태 & 백그라운드
    // startService() 나 startForegroundService()를 호출할 때 실행
    // 실행 시 서비스는 시작 상태가 되고 백그라운드에 존재할 수 있음
    // 콜백 함수 / 서비스 종료 시 어떻게 유지할 것인지
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY // 서비스 중단 시, 서비스 재실행 / onStartCommand 함수 호출
        // START NOT STICKY 서비스 재실행 X
        // START REDELIVER INTENT 서비스 재실행 / 종료 전 마지막 전달된 인텐트 재전달 -> 반드시 명령 수행할 경우
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun startForegroundService() {  // 알림 채널 생성, startForeground() 실행
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as
                    NotificationManager
            val mChannel = NotificationChannel("CHANNEL_ID",
                "CHANNEL_NAME", NotificationManager.IMPORTANCE_DEFAULT) // 알림 채널 생성
            notificationManager.createNotificationChannel(mChannel)
        }
        // 알림 생성
        val notification: Notification = Notification.Builder(this, "ChANNEL_ID")
            .setSmallIcon(R.drawable.ic_play)
            .setContentTitle("뮤직 플레이어 앱")
            .setContentText("앱이 실행 중 입니다")
            .build()
        startForeground(1, notification) // 인수로 알림 ID(식별자)와 알림 지정
    }
    // 서비스 종료
    // 상태표시줄 알림 삭제
    override fun onDestroy() {
        super.onDestroy()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        }
    }

    // 재생 중 여부
    fun isplaying() : Boolean {
        return (mMediaPlayer != null && mMediaPlayer?.isPlaying ?: false)
    }

    fun play() {    // 재생
        // 음악이 재생 중이 아닐 경우
        if (mMediaPlayer == null) {
            // 음악 파일의 리소스를 가져와 미디어 플레이어 객체를 할당해줍니다.
            mMediaPlayer = MediaPlayer.create(this,R.raw.fashion)

            mMediaPlayer?.setVolume(1.0f, 1.0f)
            mMediaPlayer?.isLooping = true
            mMediaPlayer?.start()
        } else {    // 재생 중인 경우
            if (mMediaPlayer!!.isPlaying) {
                Toast.makeText(this, "이미 음악이 실행 중입니다.", Toast.LENGTH_SHORT).show()
            } else {    // 일시 정지인 경우
                mMediaPlayer?.start()
            }
        }
    }

    fun pause() {   // 일시정지
        mMediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
            }
        }
    }

    fun stop() {    // 완전 정지
        mMediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
                it.release()    // 미디어 플레이어에 할당된 자원 해제
                mMediaPlayer = null
            }
        }
    }

}