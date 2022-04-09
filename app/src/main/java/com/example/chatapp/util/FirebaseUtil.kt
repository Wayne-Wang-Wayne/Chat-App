package com.example.chatapp.util

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.chatapp.R
import com.example.chatapp.allPage.chatActivity.ChatActivity
import com.example.chatapp.allPage.chatActivity.ChatActivity.Companion.voiceRecordEnable
import com.example.chatapp.allPage.joinChannelsFT.OnJoinSuccess
import com.example.chatapp.allPage.logInActivity.LogInActivity
import com.example.chatapp.allPage.mainActivity.MainActivity
import com.example.chatapp.allPage.splash.SplashActivity.Companion.allUserProfileUrl
import com.example.chatapp.model.*
import com.example.chatapp.util.IntentUtil.intentToAnyClass
import com.example.chatapp.util.SharedPreferenceUtil.AUTO_LOGIN
import com.example.chatapp.util.SmallUtil.getCurrentDateString
import com.example.chatapp.util.SmallUtil.getCurrentTimeStamp
import com.example.chatapp.util.SmallUtil.getCurrentTimeString
import com.example.chatapp.util.SmallUtil.glideProfileUtil
import com.example.chatapp.util.SmallUtil.simpleDialogUtilWithY
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import kotlinx.coroutines.tasks.await

class FirebaseUtil {
    companion object {
        val mFirebaseAuthInstance = FirebaseAuth.getInstance()
        val mFirebaseRTDbInstance = FirebaseDatabase.getInstance().reference
        val mFirebaseStorageInstance = FirebaseStorage.getInstance().reference
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
            val sharedPreferenceUtil = SharedPreferenceUtil(activity)
            sharedPreferenceUtil.remove(AUTO_LOGIN)
            intentToAnyClass(context = activity, cls = LogInActivity::class.java)
            activity.finish()
            mFirebaseAuthInstance.signOut()
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
            onMessageSent: ChatActivity.OnMessageSent,
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
                                        if (userUid == mFirebaseAuthInstance.currentUser?.uid){
                                            mFirebaseRTDbInstance.child(USER_CHANNELS)
                                                .child(userUid!!).child(channelUID).setValue(
                                                    UserChannels(
                                                        channelUID,
                                                        channelName,
                                                        getCurrentTimeStamp(),
                                                        getCurrentTimeString(),
                                                        getCurrentDateString(),
                                                        messageObject.message,
                                                        messageObject.imageUri,
                                                        messageObject.videoUri,
                                                        messageObject.voiceUri,
                                                        currentUserName,
                                                        mFirebaseAuthInstance.currentUser?.uid,
                                                        false
                                                    )
                                                )
                                        }else{
                                            mFirebaseRTDbInstance.child(USER_CHANNELS)
                                                .child(userUid!!).child(channelUID).setValue(
                                                    UserChannels(
                                                        channelUID,
                                                        channelName,
                                                        getCurrentTimeStamp(),
                                                        getCurrentTimeString(),
                                                        getCurrentDateString(),
                                                        messageObject.message,
                                                        messageObject.imageUri,
                                                        messageObject.videoUri,
                                                        messageObject.voiceUri,
                                                        currentUserName,
                                                        mFirebaseAuthInstance.currentUser?.uid,
                                                        true
                                                    )
                                                )
                                        }
                                    }
                                    //成功後清空editText box
                                    onMessageSent.doOnMessageSent()
                                    voiceRecordEnable = true
                                    //成功後推播訊息給其他群組成員
                                    var body = ""
                                    when {
                                        messageObject.message != "" -> {
                                            body = "$currentUserName:${messageObject.message!!}"
                                        }
                                        messageObject.imageUri != "" -> {
                                            body = "${messageObject.senderName}傳送了圖片。"
                                        }
                                        messageObject.videoUri != "" -> {
                                            body = "${messageObject.senderName}傳送了影片。"
                                        }
                                        messageObject.voiceUri != "" -> {
                                            body = "${messageObject.senderName}傳送了語音訊息。"
                                        }
                                    }
                                    FirebaseMessageService().sendFirebaseMessageWithVolley(
                                        mContext, channelUID,
                                        channelName, body
                                    )
                                }.addOnFailureListener {
                                    onMessageSent.doOnMessageSent()
                                    simpleDialogUtilWithY(mContext, "錯誤", "訊息傳送失敗，請確認網路再重試！")
                                    voiceRecordEnable = true
                                }
                        }.addOnFailureListener {
                            onMessageSent.doOnMessageSent()
                            simpleDialogUtilWithY(mContext, "錯誤", "訊息傳送失敗，請確認網路再重試！")
                            voiceRecordEnable = true
                        }
                }.addOnFailureListener {
                    onMessageSent.doOnMessageSent()
                    simpleDialogUtilWithY(mContext, "錯誤", "訊息傳送失敗，請確認網路再重試！")
                    voiceRecordEnable = true
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

        fun leaveChannel(activity: Activity, channelUID: String) {
            //依序移除相關退出群組要移除的資料
            mFirebaseRTDbInstance.child(CHANNELS).child(channelUID).child(MEMBERS).child(
                mFirebaseAuthInstance.currentUser?.uid!!
            ).removeValue().addOnSuccessListener {
                mFirebaseRTDbInstance.child(USER_CHANNELS)
                    .child(mFirebaseAuthInstance.currentUser?.uid!!).child(channelUID).removeValue()
                    .addOnSuccessListener {
                        mFirebaseRTDbInstance.child(PUBLIC_CHANNELS).child(channelUID).get()
                            .addOnSuccessListener { snapShot1 ->
                                //如果是public 群組 也有資料要更新
                                if (snapShot1.value != null) {
                                    mFirebaseRTDbInstance.child(PUBLIC_CHANNELS).child(channelUID)
                                        .child("userAmount").get()
                                        .addOnSuccessListener { snapShot2 ->
                                            var userAmount = snapShot2.getValue(Int::class.java)
                                            if (userAmount != null) {
                                                mFirebaseRTDbInstance.child(PUBLIC_CHANNELS)
                                                    .child(channelUID).child("userAmount")
                                                    .setValue(userAmount - 1).addOnSuccessListener {
                                                        activity.finish()
                                                        FirebaseMessageService().unSubscribeTopic(
                                                            channelUID
                                                        )
                                                    }
                                            }
                                        }
                                } else {
                                    //不是public群組
                                    activity.finish()
                                    FirebaseMessageService().unSubscribeTopic(channelUID)
                                }
                            }
                    }
            }
        }

        fun uploadProfileImage(
            activity: Activity,
            context: Context,
            imageView: ImageView,
            profileImageUri: Uri,
            loadingView: View? = null
        ) {
            loadingView?.visibility = View.VISIBLE
            val fireRef =
                mFirebaseStorageInstance.child("users/${mFirebaseAuthInstance.currentUser?.uid}/profile.jpg")
            fireRef.putFile(profileImageUri).addOnSuccessListener {
                fireRef.downloadUrl.addOnSuccessListener { uri ->
                    mFirebaseRTDbInstance.child(ALL_USER)
                        .child(mFirebaseAuthInstance.currentUser?.uid!!).child("userPhotoUrl")
                        .setValue(uri.toString()).addOnSuccessListener {
                            if (!activity.isDestroyed) {
                                allUserProfileUrl[mFirebaseAuthInstance.currentUser?.uid!!] = uri
                                glideProfileUtil(context, 600, uri, imageView)
                                Toast.makeText(context, "更新頭貼成功！", Toast.LENGTH_SHORT)
                                loadingView?.visibility = View.INVISIBLE
                            }
                        }
                }
            }.addOnFailureListener {
                Toast.makeText(context, "更新頭貼失敗！", Toast.LENGTH_SHORT)
                loadingView?.visibility = View.INVISIBLE
            }
        }

        fun getPictureFromFirebase(
            context: Context, imageView: ImageView
        ) {
            if (allUserProfileUrl[mFirebaseAuthInstance.currentUser?.uid] != null) {
                glideProfileUtil(
                    context,
                    600,
                    allUserProfileUrl[mFirebaseAuthInstance.currentUser?.uid]!!,
                    imageView
                )
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
                                            "",
                                            "", "", "",
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