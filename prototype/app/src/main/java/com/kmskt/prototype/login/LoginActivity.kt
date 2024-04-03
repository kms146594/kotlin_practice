package com.kmskt.prototype.login

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.prototype.R
import com.example.prototype.databinding.ActivityLoginBinding
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import com.kmskt.prototype.MainActivity

class LoginActivity: AppCompatActivity() {
    val TAG = "LoginActivity"
    private lateinit var binding: ActivityLoginBinding
    private val loginViewModel: LoginViewModel by viewModels()

    // 카카오 로그인
    var callback: (OAuthToken?, Throwable?) -> Unit = { tocken, error ->
        if (error != null) {
            Log.e(TAG, "카카오 로그인 실패")
        } else if (tocken != null) {
            Log.i(TAG, "카카오 로그인 성공")
            startActivity(Intent(this, MainActivity::class.java))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)
        binding.viewModel = loginViewModel
        binding.activity = this
        binding.lifecycleOwner = this

        setObserve()

        // 카카오 로그인
        binding.ibtnKakao.setOnClickListener {
            kakaoLogin()
        }

    }

    fun setObserve() {
        loginViewModel.showMainActivity.observe(this){
            if (it) {
                startActivity(Intent(this, MainActivity::class.java))
            }
        }
        loginViewModel.showSignupActivity.observe(this){
            if (it) {
                startActivity(Intent(this, SignupActivity::class.java))
            }
        }
        loginViewModel.showFindIdActivity.observe(this){
            if (it) {
                startActivity(Intent(this, FIndIdActivity::class.java))
            }
        }
        loginViewModel.showFindPwdActivity.observe(this){
            if (it) {
                startActivity(Intent(this, FIndPwdActivity::class.java))
            }
        }
    }

    fun kakaoLogin() {
        // 카카오톡 설치 시 카카오톡으로 로그인, 아닌 경우 카카오계정으로 로그인
        if (UserApiClient.instance.isKakaoTalkLoginAvailable(this)) {
            UserApiClient.instance.loginWithKakaoTalk(this) { token, error ->
                if (error != null) {
                    Log.e(TAG, "카카오톡으로 로그인 실패", error)

                    // 사용자가 카카오톡 설치 후 권한 요청 화면에서 로그인을 취소
                    // 의도적 로그인으로 간주하여 로그인 시도 없이 로그인 취소로 처리
                    if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                        return@loginWithKakaoTalk
                    }
                    // 카카오톡에 연결된 계정이 없는 경우, 카카오 계정으로 로그인
                    UserApiClient.instance.loginWithKakaoAccount(this, callback = callback)
                } else if (token != null) {
                    Log.i(TAG, "카카오톡으로 로그인 성공 ${token.accessToken}")
                    startActivity(Intent(this, MainActivity::class.java))
                }
            }
        } else {
            UserApiClient.instance.loginWithKakaoAccount(this, callback = callback)
        }
    }

    fun signupEmail() {
        println("signupEmail")
        loginViewModel.showSignupActivity.value = true
    }

    fun findId() {
        println("findId")
        loginViewModel.showFindIdActivity.value = true
    }

    fun findPwd() {
        println("findPw")
        loginViewModel.showFindPwdActivity.value = true
    }
}