package com.kmskt.prototype.login

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class FindPwdViewModel: ViewModel() {
    // var auth
    var id = ""
    var toastMessage = MutableLiveData("")


    fun findPwd(){
        /*
        auth.sendPasswordResetEmail(id).addOnCompleteListener {
            if (it.isSuccessful) {
                toastMessage.value = "비밀번호를 초기화했습니다."
            } else {
                toastMessage.value = "일치하는 이메일이 없습니다."
            }
        }
         */
    }
}