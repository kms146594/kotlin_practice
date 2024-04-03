package com.kmskt.prototype.login

import android.app.Application
import com.kakao.sdk.common.KakaoSdk

class GlobalApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        KakaoSdk.init(this, "0197f36c9ce6127b12a83c5fed84115d")
    }
}