package com.kmskt.prototype

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.prototype.R
import com.example.prototype.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)


    }
}