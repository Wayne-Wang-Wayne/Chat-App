package com.example.chatapp.splash

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.example.chatapp.R
import com.example.chatapp.logInActivity.LogInActivity
import com.example.chatapp.util.IntentUtil.intentToAnyClass

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        delayForThreeSecond()
    }

    private fun delayForThreeSecond() {
        val handler = Handler()
        val runnable = Runnable {
            intentToAnyClass(context = this, cls = LogInActivity::class.java)
            finish()
        }
        handler.postDelayed(runnable, 3000)
    }
}