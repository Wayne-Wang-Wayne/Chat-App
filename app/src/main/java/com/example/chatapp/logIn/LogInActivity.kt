package com.example.chatapp.logIn

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import com.example.chatapp.R
import com.example.chatapp.mainActivity.MainActivity
import com.example.chatapp.signUp.SignUpActivity
import com.example.chatapp.util.SmallUtil
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_log_in.*


class LogInActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_in)

        mAuth = FirebaseAuth.getInstance()
        btn_sign_up.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
        btn_log_in.setOnClickListener {
            val email = edt_email.text.toString()
            val password = edt_password.text.toString()
            checkIfIfoCorrect(email, password)
        }
    }

    override fun onBackPressed() {
        if (SmallUtil.isDoubleClick()) {
            finish()
        } else {
            SmallUtil.quickToast(this, "請再按一次以退出App")
        }
    }

    private fun logIn(email: String, password: String) {
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    SmallUtil.quickToast(this,"登入成功！")
                } else {
                    SmallUtil.quickToast(this, "此用戶不存在，請洽瑋瑋！")
                }
            }
    }

    private fun checkIfIfoCorrect(email: String, password: String) {
        if (email.trim().isEmpty() && password.trim().isEmpty()) {
            SmallUtil.quickToast(this, "請輸入email和密碼！")
            return
        }
        if (email.trim().isEmpty()) {
            SmallUtil.quickToast(this, "請輸入email！")
            return
        }
        if (password.trim().isEmpty()) {
            SmallUtil.quickToast(this, "請輸入密碼！")
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            SmallUtil.quickToast(this, "請輸入正確格式的email")
            return
        }
        if (!SmallUtil.isValidPassword(password)) {
            SmallUtil.quickToast(this, "密碼請輸入大於6位的小寫英數字(至少含一小寫字母和一數字)")
            return
        }
        logIn(email, password)
    }
}