package com.example.chatapp.logIn

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import com.example.chatapp.R
import com.example.chatapp.signUp.SignUpActivity
import com.example.chatapp.util.SmallUtil
import kotlinx.android.synthetic.main.activity_log_in.*
import kotlinx.android.synthetic.main.activity_sign_up.*

class LogInActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_in)

        btn_sign_up.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onBackPressed() {
        if (SmallUtil.isDoubleClick()) {
            finish()
        } else {
            Toast.makeText(this, "請再按一次以退出App", Toast.LENGTH_SHORT).show()
        }
    }
}