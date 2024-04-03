package com.kmskt.prototype.login

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SignupViewModel: ViewModel() {
    var id: MutableLiveData<String> = MutableLiveData("")
    var password: MutableLiveData<String> = MutableLiveData("")
    var signupComplete = MutableLiveData(false)

    fun signupEmail(){

    }
}