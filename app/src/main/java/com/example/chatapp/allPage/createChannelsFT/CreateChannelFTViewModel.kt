package com.example.chatapp.allPage.createChannelsFT

import android.app.Application
import android.content.Context
import androidx.annotation.NonNull
import androidx.lifecycle.MutableLiveData
import com.example.chatapp.allPage.mainActivity.BaseAndroidViewModel
import com.example.chatapp.model.ChannelInfo
import com.example.chatapp.model.OnlyUserUid
import com.example.chatapp.model.PublicChannels
import com.example.chatapp.model.UserChannels
import com.example.chatapp.util.FirebaseUtil
import com.example.chatapp.util.FirebaseUtil.Companion.CHANNELS
import com.example.chatapp.util.FirebaseUtil.Companion.MEMBERS
import com.example.chatapp.util.FirebaseUtil.Companion.PUBLIC_CHANNELS
import com.example.chatapp.util.FirebaseUtil.Companion.USER_CHANNELS
import com.example.chatapp.util.FirebaseUtil.Companion.mFirebaseRTDbInstance
import com.example.chatapp.util.SmallUtil
import com.example.chatapp.util.SmallUtil.getCurrentDateString
import com.example.chatapp.util.SmallUtil.getCurrentTimeStamp
import com.example.chatapp.util.SmallUtil.getCurrentTimeString

class CreateChannelFTViewModel(@NonNull application: Application) :
    BaseAndroidViewModel(application) {
    private val TAG: String = javaClass.simpleName
    private val uidTextLiveData = MutableLiveData<String>()
    private val nameTextLiveData = MutableLiveData<String>()
    val ifCreateSuccessfully = MutableLiveData<Boolean>()
    val ifCreateFail = MutableLiveData<Boolean>()

    fun createChannelProcess(
        mContext: Context,
        channelUid: String,
        channelName: String,
        isPublic: Boolean
    ) {
        createChannel(mContext, channelUid, channelName, isPublic)
    }

    fun uidEditTextChanged(newText: String) {
        if (newText == uidTextLiveData.value) {
            return
        }
        uidTextLiveData.value = newText
    }

    fun nameEditTextChanged(newText: String) {
        if (newText == uidTextLiveData.value) {
            return
        }
        nameTextLiveData.value = newText
    }

    private fun createChannel(
        mContext: Context,
        channelUid: String,
        channelName: String,
        isPublic: Boolean
    ) {
        mFirebaseRTDbInstance.child(CHANNELS).get()
            .addOnSuccessListener { snapShot ->
                for (postSnapShot in snapShot.children) {
                    val uid = postSnapShot.key
                    if (channelUid == uid) {
                        SmallUtil.quickToast(mContext, "此Uid已經有人使用，請更換！")
                        return@addOnSuccessListener
                    }
                }

                if (channelUid.trim().isEmpty() && channelName.trim().isEmpty()) {
                    SmallUtil.quickToast(mContext, "請輸入Uid和房名！")
                    return@addOnSuccessListener
                }

                if (channelUid.trim().isEmpty()) {
                    SmallUtil.quickToast(mContext, "請輸入Uid！")
                    return@addOnSuccessListener
                }

                if (channelName.trim().isEmpty()) {
                    SmallUtil.quickToast(mContext, "請輸入房名！")
                    return@addOnSuccessListener
                }
                if (!SmallUtil.isValidChannelUid(channelUid)) {
                    SmallUtil.quickToast(mContext, "Uid請輸入四位以上小寫英數字，不能重複！")
                    return@addOnSuccessListener
                }
                storeChannelInfo(mContext, channelUid, channelName, isPublic)
            }
    }


    private fun storeChannelInfo(
        mContext: Context,
        channelUid: String,
        channelName: String,
        isPublic: Boolean
    ) {
        //先存channel資料
        val channelInfo = ChannelInfo("", channelName, isPublic)
        val currentUserUid = FirebaseUtil.mFirebaseAuthInstance.currentUser?.uid
        //先存頻道
        mFirebaseRTDbInstance.child(CHANNELS).child(channelUid).setValue(channelInfo)
            .addOnSuccessListener {
                mFirebaseRTDbInstance.child(CHANNELS).child(channelUid)
                    .child(MEMBERS)
                    .child(currentUserUid!!)
                    .setValue(OnlyUserUid(currentUserUid))
                    .addOnSuccessListener {
                        //再存各user各自擁有的頻道
                        val userChannelModel = UserChannels(channelUid, channelName,getCurrentTimeStamp(),
                            getCurrentTimeString(),
                            getCurrentDateString(),"",false)
                        mFirebaseRTDbInstance.child(USER_CHANNELS)
                            .child(currentUserUid!!).child(channelUid).setValue(userChannelModel)
                            .addOnSuccessListener {
                                //再存公開頻道
                                if (isPublic) {
                                    val publicChannels = PublicChannels(channelUid, channelName, 1)
                                    mFirebaseRTDbInstance.child(PUBLIC_CHANNELS)
                                        .child(channelUid).setValue(publicChannels)
                                        .addOnSuccessListener {
                                            //show successful toast and clear edittext
                                            ifCreateSuccessfully.value = true
                                        }.addOnFailureListener {
                                            //error handle要清資料和show toast
                                            ifCreateFail.value = true
                                        }

                                } else {
                                    //show successful toast and clear edittext
                                    ifCreateSuccessfully.value = true
                                }
                            }.addOnFailureListener {
                                //error handle要清資料和show toast
                                ifCreateFail.value = true
                            }
                    }.addOnFailureListener {
                        //error handle要清資料和show toast
                        ifCreateFail.value = true
                    }
            }.addOnFailureListener {
                //error handle要清資料和show toast
                ifCreateFail.value = true
            }
    }
}