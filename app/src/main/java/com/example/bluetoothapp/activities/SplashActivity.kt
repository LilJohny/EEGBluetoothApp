package com.example.bluetoothapp.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import com.example.bluetoothapp.R

/**
 * Created by AbhiAndroid
 */
class SplashActivity : Activity() {
    var handler: Handler? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_screen)
        handler = Handler()
        handler!!.postDelayed({
            val intent = Intent(this@SplashActivity, EntryActivity::class.java)
            startActivity(intent)
            finish()
        }, 3000)
    }
}