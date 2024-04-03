package com.kmskt.prototype.login

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient

class AuthHandlerActivity : AppCompatActivity() {

    private val mCallbcak: (OAuthToken?, Throwable?) -> Unit = { tocken, error ->
        if (error != null) {
            Log.e(TAG, "로그인 실패")
        } else if (tocken != null){
            Log.e(TAG, "로그인 성공")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 카카오톡 설치 확인
        if (UserApiClient.instance.isKakaoTalkLoginAvailable(this)) {
            // 로그인
            UserApiClient.instance.loginWithKakaoTalk(this) { token, error ->
                // 실패
                if (error != null) {
                    Log.e(TAG, "카카오톡 로그인 실패", error)
                    // 로그인 시도 없이 사용자가 의도적으로 취소
                    if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                        return@loginWithKakaoTalk
                    }
                    // 다른 오류
                    else {
                        // 카카오톡에 연결된 계정이 없는 경우, 카카오계정 로그인
                        UserApiClient.instance.loginWithKakaoAccount(
                            this, callback = mCallbcak
                        )   // 카카오 계정 로그인
                    }
                }
                // 로그인 성공
                else if (token != null){
                    Log.e(TAG, "로그인 성공 ${token.accessToken}")
                }
            }
        } else {
            // 카카오 계정 로그인
            UserApiClient.instance.loginWithKakaoAccount(this, callback = mCallbcak)
        }
    }
}