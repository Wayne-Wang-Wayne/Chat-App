package com.example.chatapp.allPage.splash

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.example.chatapp.R
import com.example.chatapp.allPage.logInActivity.LogInActivity
import com.example.chatapp.util.FirebaseUtil
import com.example.chatapp.util.IntentUtil.intentToAnyClass
import com.example.chatapp.util.SharedPreferenceUtil
import com.example.chatapp.util.SmallUtil

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        delayForThreeSecond()
    }

    private fun delayForThreeSecond() {
        checkIsAutoLogIn()
    }

    override fun onBackPressed() {
        if (SmallUtil.isDoubleClick()) {
            finish()
        } else {
            SmallUtil.quickToast(this, "請再按一次以退出App")
        }
    }

    private fun checkIsAutoLogIn() {
        val sharedPreferenceUtil = SharedPreferenceUtil(this)
        val autoLoginInfo = sharedPreferenceUtil.getListString(SharedPreferenceUtil.AUTO_LOGIN)
        val handler = Handler()
        var runnable: Runnable = if (autoLoginInfo.size == 2) {
            Runnable {
                FirebaseUtil.checkLogInfoAndLogIn(
                    this,
                    this,
                    autoLoginInfo[0],
                    autoLoginInfo[1]
                )
            }
        } else {
            Runnable {
                intentToAnyClass(context = this, cls = LogInActivity::class.java)
                finish()
            }
        }
        handler.postDelayed(runnable, 3000)
    }
}