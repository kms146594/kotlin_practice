package com.example.musicplayer

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.musicplayer.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar
import java.security.Permission

class MainActivity : AppCompatActivity() {

    private lateinit var layout: View
    val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    // 서비스가 액티비티와 연결되지 않음
    var mService: MusicPlayerService? = null
    @RequiresApi(Build.VERSION_CODES.P)
    val permission = android.Manifest.permission.FOREGROUND_SERVICE



    // 액티비티에서 바인드 된 서비스 제공
    // 서비스와 구성요소 연결 상태 모니터링
    private val mServiceConnection = object : ServiceConnection {
        // 서비스 연결 성공
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            // MusicPlayerBinder로 형변환
            mService = (service as MusicPlayerService.MusicPlayerBinder).getService()
        }

        // 서비스 연결 실패
        override fun onServiceDisconnected(name: ComponentName?) {
            mService = null // 서비스가 끊기면, mService를 null로
        }
    }

    /*
        private fun bindService() {
            val intent = Intent(this, AudioPlayerService::class.java)
            bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE)
        }
         */


    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view = binding.root
        layout = binding.mainLayout
        setContentView(view)



        binding.button.setOnClickListener {
            onClickRequestPermission(view)
        }

        binding.btnPlay.setOnClickListener {
            play()
        }

        binding.btnPause.setOnClickListener {
            pause()
        }

        binding.btnStop.setOnClickListener {
            stop()
        }
    }

    override fun onResume() {
        super.onResume()

        if (mService == null) {
            // 안드로이드 O이상이면, startForegroundService를 사용해야함.
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(Intent(this, MusicPlayerService::class.java))
            } else {
                startService(Intent(applicationContext, MusicPlayerService::class.java))
            }

            // 액티비티를 서비스와 바인드 시킴
            val intent = Intent(this, MusicPlayerService::class.java)
            // 서비스와 바인드
            bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onPause() {
        super.onPause()

        // 사용자가 액티비티를 떠났을 때 처리
        if (mService != null) {
            if (!mService!!.isplaying()) {  // mService가 재생되고 있지 않다면
                mService!!.stopSelf()   // 서비스를 중단
            }
            unbindService(mServiceConnection)   // 서비스로부터 연결을 끊음
            mService = null
        }
    }

    private fun play() {
        mService?.play()
    }

    private fun pause() {
        mService?.pause()
    }

    private fun stop() {
        mService?.stop()
    }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                Log.i("Permission: ", "Granted")
            } else {
                Log.i("Permission: ", "Denied")
            }
        }


    @RequiresApi(Build.VERSION_CODES.P)
    fun onClickRequestPermission(view: View) {
        when {
            ContextCompat.checkSelfPermission(
                this,
                permission
            ) == PackageManager.PERMISSION_GRANTED -> {
                layout.showSnackbar(
                    view,
                    getString(R.string.permission_granted),
                    Snackbar.LENGTH_INDEFINITE,
                    null
                ) {}
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                permission
            ) -> {
                layout.showSnackbar(
                    view,
                    getString(R.string.permission_required),
                    Snackbar.LENGTH_INDEFINITE,
                    getString(R.string.ok)
                ) {
                    requestPermissionLauncher.launch(
                        permission
                    )
                }
            }

            else -> {
                requestPermissionLauncher.launch(
                    permission
                )
            }
        }
    }

}

fun View.showSnackbar(
    view: View,
    msg: String,
    length: Int,
    actionMessage: CharSequence?,
    action: (View) -> Unit
) {
    val snackbar = Snackbar.make(view, msg, length)
    if (actionMessage != null) {
        snackbar.setAction(actionMessage) {
            action(this)
        }.show()
    } else {
        snackbar.show()
    }
}