package com.example.chatapp.util

import android.app.Activity
import android.content.Context
import com.example.chatapp.allPage.logInActivity.LogInActivity
import com.example.chatapp.allPage.mainActivity.MainActivity
import com.example.chatapp.model.Message
import com.example.chatapp.model.User
import com.example.chatapp.util.IntentUtil.intentToAnyClass
import com.example.chatapp.util.SharedPreferenceUtil.AUTO_LOGIN
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
            mFirebaseRTDbInstance.child("user").get().addOnSuccessListener { snapShot ->
                for (postSnapShot in snapShot.children) {
                    val user = postSnapShot.getValue(User::class.java)
                    if (name == user?.name) {
                        SmallUtil.quickToast(mContext, "此名稱已經有人使用，請更換名稱！")
                        return@addOnSuccessListener
                    }
                }

                if (email.trim().isEmpty() && password.trim().isEmpty() && name.trim().isEmpty()) {
                    SmallUtil.quickToast(mContext, "請輸入email和密碼！")
                    return@addOnSuccessListener
                }
                if (name.trim().isEmpty()) {
                    SmallUtil.quickToast(mContext, "請輸入ID！")
                    return@addOnSuccessListener
                }
                if (email.trim().isEmpty()) {
                    SmallUtil.quickToast(mContext, "請輸入email！")
                    return@addOnSuccessListener
                }
                if (password.trim().isEmpty()) {
                    SmallUtil.quickToast(mContext, "請輸入密碼！")
                    return@addOnSuccessListener
                }
                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    SmallUtil.quickToast(mContext, "請輸入正確格式的email")
                    return@addOnSuccessListener
                }
                if (!SmallUtil.isValidPassword(password)) {
                    SmallUtil.quickToast(mContext, "密碼請輸入大於6位的小寫英數字(至少含一小寫字母和一數字)")
                    return@addOnSuccessListener
                }
                signUp(activity, mContext, name, email, password)
            }

        }

        //call for logging out
        fun logOut(activity: Activity) {
            mFirebaseAuthInstance.signOut()
            val sharedPreferenceUtil = SharedPreferenceUtil(activity)
            sharedPreferenceUtil.remove(AUTO_LOGIN)
            intentToAnyClass(context = activity, cls = LogInActivity::class.java)
            activity.finish()
        }

        fun listenToRTDBForUser(path: String, valueEventListener: ValueEventListener) {
            mFirebaseRTDbInstance.child(path).addValueEventListener(valueEventListener)
        }

        fun storeMessageToDB(senderRoomId: String, receiverRoomId: String, messageObject: Message) {
            mFirebaseRTDbInstance.child("chats").child(senderRoomId!!).child("message").push()
                .setValue(messageObject).addOnSuccessListener {
                    mFirebaseRTDbInstance.child("chats").child(receiverRoomId!!).child("message")
                        .push()
                        .setValue(messageObject)
                }

        }

        fun listenToRTDBForMessage(
            firstPath: String,
            secondPath: String,
            thirdPath: String,
            valueEventListener: ValueEventListener
        ) {
            mFirebaseRTDbInstance.child(firstPath).child(secondPath).child(thirdPath)
                .addValueEventListener(valueEventListener)
        }

        private fun logIn(activity: Activity, mContext: Context, email: String, password: String) {
            mFirebaseAuthInstance.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(activity) { task ->
                    if (task.isSuccessful) {
                        storeAutoLoginInfo(email, password, mContext)
                        intentToAnyClass(context = activity, cls = MainActivity::class.java)
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
                        storeAutoLoginInfo(email, password, mContext)
                        addUserToDatabase(name, email, mFirebaseAuthInstance.currentUser?.uid!!)
                        intentToAnyClass(context = activity, cls = MainActivity::class.java)
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

        private fun storeAutoLoginInfo(email: String, password: String, mContext: Context) {
            val accountInfo = ArrayList<String>()
            accountInfo.add(email)
            accountInfo.add(password)
            val sharedPreferenceUtil =
                SharedPreferenceUtil(
                    mContext
                )
            sharedPreferenceUtil.putListString(AUTO_LOGIN, accountInfo)
        }


    }


}