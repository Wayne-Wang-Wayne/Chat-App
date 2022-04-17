package com.example.chatapp.allPage.logInActivity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import com.example.chatapp.R
import com.example.chatapp.customStuff.SafeClickListener.Companion.setSafeOnClickListener
import com.example.chatapp.signUpActivity.SignUpActivity
import com.example.chatapp.util.FirebaseUtil.Companion.checkLogInfoAndLogIn
import com.example.chatapp.util.IntentUtil.intentToAnyClass
import com.example.chatapp.util.SharedPreferenceUtil
import com.example.chatapp.util.SharedPreferenceUtil.AUTO_LOGIN
import com.example.chatapp.util.SmallUtil
import kotlinx.android.synthetic.main.activity_log_in.*


class LogInActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_in)
        //customize status bar
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        setUpViewLogic()
    }

    private var doubleBackToExitPressedOnce = false
    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed()
            return
        }
        this.doubleBackToExitPressedOnce = true
        SmallUtil.quickToast(this, "請再按一次以退出App")
        Handler(Looper.getMainLooper()).postDelayed(Runnable { doubleBackToExitPressedOnce = false }, 2000)
    }

    private fun setUpViewLogic() {
        btn_sign_up.setSafeOnClickListener {
            intentToAnyClass(context = this, cls = SignUpActivity::class.java)
        }
        btn_log_in.setSafeOnClickListener {
            val email = edt_email.text.toString()
            val password = edt_password.text.toString()
            checkLogInfoAndLogIn(this, this, email, password)
        }
    }




}