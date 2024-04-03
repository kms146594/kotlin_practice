package com.kmskt.prototype.login

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import com.example.prototype.R
import com.example.prototype.databinding.ActivityFindPwdBinding

class FIndPwdActivity : AppCompatActivity() {
    lateinit var binding: ActivityFindPwdBinding
    val findPwdViewModel: FindPwdViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_find_pwd)
        binding.activity = this
        binding.viewModel = findPwdViewModel
        binding.lifecycleOwner = this

        setObserve()
    }

    fun setObserve() {
        findPwdViewModel.toastMessage.observe(this) {
            if (!it.isEmpty()){
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        }
    }
}