package com.example.chatapp.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.example.chatapp.logIn.LogInActivity
import com.example.chatapp.mainActivity.MainActivity
import com.example.chatapp.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class FirebaseUtil {
    companion object {
        val mFirebaseAuthInstance = FirebaseAuth.getInstance()
        val mFirebaseRTDbInstance = FirebaseDatabase.getInstance().reference

        //call for logging in
        fun checkLogInfoAndLogIn(
            activity: Activity,
            mContext: Context,
            email: String,
            password: String
        ) {
            if (email.trim().isEmpty() && password.trim().isEmpty()) {
                SmallUtil.quickToast(mContext, "請輸入email和密碼！")
                return
            }
            if (email.trim().isEmpty()) {
                SmallUtil.quickToast(mContext, "請輸入email！")
                return
            }
            if (password.trim().isEmpty()) {
                SmallUtil.quickToast(mContext, "請輸入密碼！")
                return
            }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                SmallUtil.quickToast(mContext, "請輸入正確格式的email")
                return
            }
            if (!SmallUtil.isValidPassword(password)) {
                SmallUtil.quickToast(mContext, "密碼請輸入大於6位的小寫英數字(至少含一小寫字母和一數字)")
                return
            }
            logIn(activity, mContext, email, password)
        }

        //call for signing up
        fun checkSingInfoAndSingUp(
            activity: Activity,
            mContext: Context,
            name: String,
            email: String,
            password: String
        ) {
            if (email.trim().isEmpty() && password.trim().isEmpty() && name.trim().isEmpty()) {
                SmallUtil.quickToast(mContext, "請輸入email和密碼！")
                return
            }
            if (name.trim().isEmpty()) {
                SmallUtil.quickToast(mContext, "請輸入ID！")
                return
            }
            if (email.trim().isEmpty()) {
                SmallUtil.quickToast(mContext, "請輸入email！")
                return
            }
            if (password.trim().isEmpty()) {
                SmallUtil.quickToast(mContext, "請輸入密碼！")
                return
            }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                SmallUtil.quickToast(mContext, "請輸入正確格式的email")
                return
            }
            if (!SmallUtil.isValidPassword(password)) {
                SmallUtil.quickToast(mContext, "密碼請輸入大於6位的小寫英數字(至少含一小寫字母和一數字)")
                return
            }
            signUp(activity, mContext, name, email, password)
        }

        //call for logging out
        fun logOut(activity: Activity) {
            mFirebaseAuthInstance.signOut()
            val intent = Intent(activity,LogInActivity::class.java)
            activity.startActivity(intent)
            activity.finish()
        }

        fun listenToRTDB(path:String,valueEventListener: ValueEventListener){
            mFirebaseRTDbInstance.child(path).addValueEventListener(valueEventListener)
        }

        private fun logIn(activity: Activity, mContext: Context, email: String, password: String) {
            mFirebaseAuthInstance.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(activity) { task ->
                    if (task.isSuccessful) {
                        val intent = Intent(activity, MainActivity::class.java)
                        activity.startActivity(intent)
                        activity.finish()
                        SmallUtil.quickToast(mContext, "登入成功！")
                    } else {
                        SmallUtil.quickToast(mContext, "此用戶不存在，請洽瑋瑋！")
                    }
                }
        }

        private fun signUp(
            activity: Activity,
            mContext: Context,
            name: String,
            email: String,
            password: String
        ) {
            mFirebaseAuthInstance.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(activity) { task ->
                    if (task.isSuccessful) {
                        //登入成功回到主頁
                        addUserToDatabase(name, email, mFirebaseAuthInstance.currentUser?.uid!!)
                        val intent = Intent(activity, MainActivity::class.java)
                        activity.startActivity(intent)
                        activity.finish()
                        SmallUtil.quickToast(mContext, "註冊成功！自動登入！")
                    } else {
                        SmallUtil.quickToast(mContext, "註冊異常，請洽瑋瑋！")
                    }
                }
        }

        private fun addUserToDatabase(name: String, email: String, uid: String) {
            mFirebaseRTDbInstance.apply {
                child("user").child(uid).setValue(User(name, email, uid))
            }
        }
    }


}