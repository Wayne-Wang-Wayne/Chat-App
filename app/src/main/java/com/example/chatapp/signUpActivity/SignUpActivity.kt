package com.example.chatapp.signUpActivity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.chatapp.R
import com.example.chatapp.customStuff.SafeClickListener.Companion.setSafeOnClickListener
import com.example.chatapp.util.FirebaseUtil.Companion.checkSingInfoAndSingUp
import kotlinx.android.synthetic.main.activity_sign_up.*

class SignUpActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        setUpViewLogic()

    }

    private fun setUpViewLogic(){
        btn_sign_up.setSafeOnClickListener {
            val name = edt_name.text.toString()
            val email = edt_email.text.toString()
            val password = edt_password.text.toString()
            checkSingInfoAndSingUp(this, this, name, email, password)
        }
    }


}