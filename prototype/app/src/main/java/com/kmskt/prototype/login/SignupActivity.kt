package com.kmskt.prototype.login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import com.example.prototype.R
import com.example.prototype.databinding.ActivitySignupBinding

class SignupActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignupBinding
    private val signupViewModel: SignupViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_signup)
        binding.activity = this
        binding.viewModel = signupViewModel
        binding.lifecycleOwner = this

        setObserve()
    }

    fun setObserve() {
        signupViewModel.signupComplete.observe(this) {
            if (it) {
                finish()
                startActivity(Intent(this, LoginActivity::class.java))
            }
        }
    }
}