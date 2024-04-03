package com.kmskt.prototype.login

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class FindIdVIewModel: ViewModel() {
    var auth = ""
    var id = ""
    var toastMessage = MutableLiveData("")

}