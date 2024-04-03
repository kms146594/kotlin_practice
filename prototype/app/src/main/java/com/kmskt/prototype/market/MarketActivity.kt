package com.kmskt.prototype.market

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.prototype.R
import com.example.prototype.databinding.ActivityMarketBinding

class MarketActivity : AppCompatActivity() {

    val binding by lazy { ActivityMarketBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)


    }
}