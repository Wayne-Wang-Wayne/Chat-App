package com.example.chatapp.logIn

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.chatapp.R
import com.example.chatapp.mainActivity.MainActivity
import com.example.chatapp.signUp.SignUpActivity
import com.example.chatapp.util.FirebaseUtil.Companion.checkLogInfoAndLogIn
import com.example.chatapp.util.FirebaseUtil.Companion.mFirebaseAuthInstance
import com.example.chatapp.util.SmallUtil
import kotlinx.android.synthetic.main.activity_log_in.*


class LogInActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_in)

        setUpViewLogic()
    }

    override fun onBackPressed() {
        if (SmallUtil.isDoubleClick()) {
            finish()
        } else {
            SmallUtil.quickToast(this, "請再按一次以退出App")
        }
    }

    private fun setUpViewLogic(){
        btn_sign_up.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
        btn_log_in.setOnClickListener {
            val email = edt_email.text.toString()
            val password = edt_password.text.toString()
            checkLogInfoAndLogIn(this, this, email, password)
        }
    }


}