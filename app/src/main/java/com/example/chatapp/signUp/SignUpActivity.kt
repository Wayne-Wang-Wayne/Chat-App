package com.example.chatapp.signUp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.chatapp.R
import com.example.chatapp.mainActivity.MainActivity
import com.example.chatapp.util.SmallUtil
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_sign_up.*

class SignUpActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        mAuth = FirebaseAuth.getInstance()
        btn_sign_up.setOnClickListener {
            val name = edt_name.text.toString()
            val email = edt_email.text.toString()
            val password = edt_password.text.toString()
            checkIfIfoCorrect(name, email, password)
        }
    }

    private fun signUp(email: String, password: String) {
        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    //登入成功回到主頁
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                    SmallUtil.quickToast(this,"註冊成功！自動登入！")
                } else {
                    SmallUtil.quickToast(this, "註冊異常，請洽瑋瑋！")
                }
            }
    }

    private fun checkIfIfoCorrect(name: String, email: String, password: String) {
        if (email.trim().isEmpty() && password.trim().isEmpty() && name.trim().isEmpty()) {
            SmallUtil.quickToast(this, "請輸入email和密碼！")
            return
        }
        if (name.trim().isEmpty()) {
            SmallUtil.quickToast(this, "請輸入ID！")
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
        signUp(email, password)
    }

}