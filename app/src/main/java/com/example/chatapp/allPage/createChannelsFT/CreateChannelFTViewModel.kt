package com.example.chatapp.allPage.createChannelsFT

import android.app.Application
import android.content.Context
import androidx.annotation.NonNull
import androidx.lifecycle.MutableLiveData
import com.example.chatapp.allPage.mainActivity.BaseAndroidViewModel
import com.example.chatapp.model.ChannelInfo
import com.example.chatapp.model.OnlyUserUid
import com.example.chatapp.util.FirebaseUtil
import com.example.chatapp.util.SmallUtil

class CreateChannelFTViewModel(@NonNull application: Application) :
    BaseAndroidViewModel(application) {
    private val TAG: String = javaClass.simpleName
    private val uidTextLiveData = MutableLiveData<String>()
    private val nameTextLiveData = MutableLiveData<String>()
    val isCreateSuccessfully = MutableLiveData<Boolean>()

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
        FirebaseUtil.mFirebaseRTDbInstance.child("channels").get()
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
        FirebaseUtil.mFirebaseRTDbInstance.child("channels").child(channelUid).setValue(channelInfo)
            .addOnSuccessListener {
                FirebaseUtil.mFirebaseRTDbInstance.child("channels").child(channelUid)
                    .child("members")
                    .push()
                    .setValue(OnlyUserUid(FirebaseUtil.mFirebaseAuthInstance.currentUser?.uid))
                    .addOnSuccessListener {
                        //show toast and clear edittext
                        isCreateSuccessfully.value = true
                    }
            }
    }
}