package com.example.chatapp.util

import android.app.Activity
import android.content.Context
import com.example.chatapp.allPage.chatActivity.OnMessageSent
import com.example.chatapp.allPage.joinChannelsFT.OnJoinSuccess
import com.example.chatapp.allPage.logInActivity.LogInActivity
import com.example.chatapp.allPage.mainActivity.MainActivity
import com.example.chatapp.model.*
import com.example.chatapp.util.IntentUtil.intentToAnyClass
import com.example.chatapp.util.SharedPreferenceUtil.AUTO_LOGIN
import com.example.chatapp.util.SmallUtil.getCurrentDateString
import com.example.chatapp.util.SmallUtil.getCurrentTimeStamp
import com.example.chatapp.util.SmallUtil.getCurrentTimeString
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.tasks.await

class FirebaseUtil {
    companion object {
        val mFirebaseAuthInstance = FirebaseAuth.getInstance()
        val mFirebaseRTDbInstance = FirebaseDatabase.getInstance().reference
        var currentUserName = ""

        // Firebase path name
        val USER_CHANNELS = "userChannels"
        val ALL_USER = "allUser"
        val CHATS = "chats"
        val MESSAGE = "message"
        val CHANNELS = "channels"
        val PUBLIC_CHANNELS = "publicChannels"
        val MEMBERS = "members"
        val CHANNEL_MESSAGES = "channelMessages"

        fun setCurrentUserName() {
            mFirebaseRTDbInstance.child(ALL_USER).child(mFirebaseAuthInstance.currentUser!!.uid)
                .get().addOnSuccessListener { snapShot ->
                    val userInfo = snapShot.getValue(User::class.java)
                    if (userInfo != null) {
                        currentUserName = userInfo.name.toString()
                    }
                }
        }

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
            mFirebaseRTDbInstance.child(ALL_USER).get().addOnSuccessListener { snapShot ->
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

        fun listenToRTDBForUser(valueEventListener: ValueEventListener) {
            mFirebaseRTDbInstance.child(USER_CHANNELS)
                .child(mFirebaseAuthInstance.currentUser!!.uid)
                .addValueEventListener(valueEventListener)
        }

        fun storeMessageToDB(
            channelUID: String,
            channelName: String,
            messageObject: ChannelMessage,
            onMessageSent: OnMessageSent,
            mContext: Context
        ) {
            //存到channelMessages
            mFirebaseRTDbInstance.child(CHANNEL_MESSAGES).child(channelUID)
                .push()
                .setValue(messageObject).addOnSuccessListener {
                    //存到channels
                    mFirebaseRTDbInstance.child(CHANNELS).child(channelUID).child("lastMessageSent")
                        .setValue(messageObject.message).addOnSuccessListener {
                            //存到每個user的userChannels
                            mFirebaseRTDbInstance.child(CHANNELS).child(channelUID).child(MEMBERS)
                                .get().addOnSuccessListener { snapShot ->
                                    //全部成功
                                    for (postSnapShot in snapShot.children) {
                                        val userUid = postSnapShot.key
                                        mFirebaseRTDbInstance.child(USER_CHANNELS)
                                            .child(userUid!!).child(channelUID).setValue(
                                                UserChannels(
                                                    channelUID,
                                                    channelName,
                                                    getCurrentTimeStamp(),
                                                    getCurrentTimeString(),
                                                    getCurrentDateString(),
                                                    messageObject.message,
                                                    currentUserName,
                                                    true
                                                )
                                            )
                                    }
                                    //成功後清空editText box
                                    onMessageSent.doOnMessageSent()
                                    //成功後推播訊息給其他群組成員
                                    FirebaseMessageService().sendFirebaseMessageWithVolley(
                                        mContext, channelUID,
                                        currentUserName, messageObject.message!!
                                    )
                                }

                        }
                }

        }

        fun listenToRTDBForMessage(channelUID: String, valueEventListener: ValueEventListener) {
            mFirebaseRTDbInstance.child(CHANNEL_MESSAGES).child(channelUID)
                .addValueEventListener(valueEventListener)
        }

        fun joinChannel(
            mContext: Context,
            channelUid: String,
            onJoinSuccess: OnJoinSuccess? = null
        ) {
            //檢查是否已經加入
            mFirebaseRTDbInstance.child(USER_CHANNELS)
                .child(mFirebaseAuthInstance.currentUser!!.uid).get()
                .addOnSuccessListener { snapShotOne ->
                    if (snapShotOne != null) {
                        for (postSnapShot in snapShotOne.children) {
                            val uid = postSnapShot.key
                            if (channelUid == uid) {
                                SmallUtil.quickToast(mContext, "您已經在此群組！")
                                return@addOnSuccessListener
                            }
                        }
                    }
                    //檢查此群組是否存在
                    mFirebaseRTDbInstance.child(CHANNELS).get()
                        .addOnSuccessListener { snapShotTwo ->
                            if (snapShotTwo == null) {
                                SmallUtil.quickToast(mContext, "此群組不存在！")
                                return@addOnSuccessListener
                            }
                            var isExisted = false
                            for (postSnapShot in snapShotTwo.children) {
                                val uid = postSnapShot.key
                                if (channelUid == uid) {
                                    isExisted = true
                                }
                            }
                            if (!isExisted) {
                                SmallUtil.quickToast(mContext, "此群組不存在！")
                                return@addOnSuccessListener
                            }

                            if (channelUid.trim().isEmpty()) {
                                SmallUtil.quickToast(mContext, "請輸入Uid！")
                                return@addOnSuccessListener
                            }

                            if (!SmallUtil.isValidChannelUid(channelUid)) {
                                SmallUtil.quickToast(mContext, "Uid請輸入四位以上小寫英數字！")
                                return@addOnSuccessListener
                            }
                            joinChannelProcess(mContext, channelUid, onJoinSuccess)
                        }
                }
        }

        fun subScribeAllMyChannelsUid() {
            mFirebaseRTDbInstance.child(USER_CHANNELS)
                .child(mFirebaseAuthInstance.currentUser!!.uid).get()
                .addOnSuccessListener { snapShot ->
                    val topicList = ArrayList<String>()
                    for (postSnapShot in snapShot.children) {
                        topicList.add(postSnapShot.key!!)
                    }
                    FirebaseMessageService().subscribeToMultipleTopic(topicList)
                }
        }

        private fun joinChannelProcess(
            mContext: Context,
            channelUid: String,
            onJoinSuccess: OnJoinSuccess? = null
        ) {
            //先set channels 裡的members成員名單
            mFirebaseRTDbInstance.child(CHANNELS).child(channelUid).child(MEMBERS)
                .child(mFirebaseAuthInstance.currentUser!!.uid)
                .setValue(OnlyUserUid(mFirebaseAuthInstance.currentUser!!.uid))
                .addOnSuccessListener {
                    //設Public Channel list(如果存在就成員人數加一)
                    mFirebaseRTDbInstance.child(PUBLIC_CHANNELS).get()
                        .addOnSuccessListener { snapShot ->
                            if (snapShot != null) {
                                for (postSnapShot in snapShot.children) {
                                    if (postSnapShot.key == channelUid) {
                                        val publicChannel =
                                            postSnapShot.getValue(PublicChannels::class.java)
                                        mFirebaseRTDbInstance.child(PUBLIC_CHANNELS)
                                            .child(channelUid).setValue(
                                                PublicChannels(
                                                    publicChannel?.channelUID,
                                                    publicChannel?.channelName,
                                                    publicChannel?.userAmount?.plus(1)
                                                )
                                            )
                                    }
                                }

                            }
                            //set userChannel 名單
                            mFirebaseRTDbInstance.child(CHANNELS).child(channelUid)
                                .child("channelName").get()
                                .addOnSuccessListener { snapShotTwo ->
                                    //找頻道暱稱
                                    var channelsName = ""
                                    if (snapShotTwo.value != null) {
                                        channelsName = snapShotTwo.value.toString()
                                    }

                                    val userChannel =
                                        UserChannels(
                                            channelUid,
                                            channelsName,
                                            getCurrentTimeStamp(),
                                            getCurrentTimeString(),
                                            getCurrentDateString(),
                                            "",
                                            "",
                                            false
                                        )
                                    //set 到 userChannel
                                    mFirebaseRTDbInstance.child(USER_CHANNELS)
                                        .child(mFirebaseAuthInstance.currentUser!!.uid)
                                        .child(channelUid).setValue(userChannel)
                                        .addOnSuccessListener {
                                            //全部set 成功

                                            //show success dialog
                                            SmallUtil.simpleDialogUtilWithY(
                                                mContext,
                                                "成功",
                                                "加入成功，趕快去聊天！"
                                            )
                                            //clear edittext box
                                            onJoinSuccess?.onJoinSuccess()
                                            //subscribe FCM topic
                                            val topicList = ArrayList<String>()
                                            topicList.add(channelUid)
                                            FirebaseMessageService().subscribeToMultipleTopic(
                                                topicList
                                            )
                                        }.addOnFailureListener {
                                            SmallUtil.quickToast(mContext, "註冊異常，煩請聯繫瑋瑋！")
                                        }
                                }.addOnFailureListener {
                                    SmallUtil.quickToast(mContext, "註冊異常，煩請聯繫瑋瑋！")
                                }
                        }.addOnFailureListener {
                            SmallUtil.quickToast(mContext, "註冊異常，煩請聯繫瑋瑋！")
                        }

                }.addOnFailureListener {
                    SmallUtil.quickToast(mContext, "註冊異常，煩請聯繫瑋瑋！")
                }
        }


        private fun logIn(activity: Activity, mContext: Context, email: String, password: String) {
            mFirebaseAuthInstance.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(activity) { task ->
                    if (task.isSuccessful) {
                        storeAutoLoginInfo(email, password, mContext)
                        intentToAnyClass(context = activity, cls = MainActivity::class.java)
                        activity.finish()
                        SmallUtil.quickToast(mContext, "登入成功！")
                        setCurrentUserName()
                    } else {
                        SmallUtil.quickToast(mContext, "登入失敗！此用戶不存在，請洽瑋瑋！")
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
                        currentUserName = name
                    } else {
                        SmallUtil.quickToast(mContext, "註冊異常，請洽瑋瑋！")
                    }
                }
        }

        private fun addUserToDatabase(name: String, email: String, uid: String) {
            mFirebaseRTDbInstance.apply {
                child(ALL_USER).child(uid).setValue(User(email, name, uid, ""))
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