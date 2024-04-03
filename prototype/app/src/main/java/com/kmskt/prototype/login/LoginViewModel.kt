package com.kmskt.prototype.login


import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    var id : MutableLiveData<String> = MutableLiveData("")
    var password : MutableLiveData<String> = MutableLiveData("")

    var showSignupActivity: MutableLiveData<Boolean> = MutableLiveData(false)
    var showFindIdActivity: MutableLiveData<Boolean> = MutableLiveData(false)
    var showFindPwdActivity: MutableLiveData<Boolean> = MutableLiveData(false)
    var showMainActivity: MutableLiveData<Boolean> = MutableLiveData(false)


    fun loginEmail(){
        println("Email_login")

        /*
        signInWithEmailAndPassword().addOnCompleteListenr{
            if (it.isSuccessful) {
                if (it.result.user?.isEmailVerified == true) {
                    showMainActivity.value = true
                } else {

                }
            }
        }
         */
    }
}