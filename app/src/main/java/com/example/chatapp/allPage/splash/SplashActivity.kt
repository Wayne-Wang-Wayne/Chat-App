package com.example.chatapp.allPage.splash

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.core.net.toUri
import com.example.chatapp.R
import com.example.chatapp.allPage.logInActivity.LogInActivity
import com.example.chatapp.model.User
import com.example.chatapp.util.FirebaseMessageService
import com.example.chatapp.util.FirebaseUtil
import com.example.chatapp.util.FirebaseUtil.Companion.ALL_USER
import com.example.chatapp.util.FirebaseUtil.Companion.mFirebaseRTDbInstance
import com.example.chatapp.util.IntentUtil.intentToAnyClass
import com.example.chatapp.util.SharedPreferenceUtil
import com.example.chatapp.util.SmallUtil

class SplashActivity : AppCompatActivity() {

    companion object {
        val allUserProfileUrl = HashMap<String, Uri>()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        mFirebaseRTDbInstance.child(ALL_USER).get().addOnSuccessListener { snapShot ->
            for (postSnapShot in snapShot.children) {
                val user = postSnapShot.getValue(User::class.java)
                if (user?.userPhotoUrl!=""){
                    allUserProfileUrl[user?.userUID!!] = user?.userPhotoUrl!!.toUri()
                }
            }
            delayForThreeSecond()
        }
    }

    private fun delayForThreeSecond() {
        checkIsAutoLogIn()
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

        handler.postDelayed(runnable, 100)
    }

    private var doubleBackToExitPressedOnce = false
    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed()
            return
        }
        this.doubleBackToExitPressedOnce = true
        SmallUtil.quickToast(this, "請再按一次以退出App")
        Handler(Looper.getMainLooper()).postDelayed(Runnable {
            doubleBackToExitPressedOnce = false
        }, 2000)
    }
}