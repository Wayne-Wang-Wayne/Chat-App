package com.example.chatapp.allPage.joinChannelsFT

import android.app.Application
import android.content.Context
import androidx.annotation.NonNull
import androidx.lifecycle.MutableLiveData
import com.example.chatapp.allPage.mainActivity.BaseAndroidViewModel
import com.example.chatapp.util.FirebaseUtil
import com.example.chatapp.util.FirebaseUtil.Companion.mFirebaseAuthInstance


class JoinChannelViewModel(@NonNull application: Application) :
    BaseAndroidViewModel(application) {

    private val uidTextLiveData = MutableLiveData<String>()
    private val ifJoinSuccess = MutableLiveData<Boolean>()

    fun uidEditTextChanged(newText: String) {
        if (newText == uidTextLiveData.value) {
            return
        }
        uidTextLiveData.value = newText
    }

    fun joinChannel(mContext: Context, channelUid: String,onJoinSuccess:OnJoinSuccess? = null) {
        FirebaseUtil.joinChannel(mContext, channelUid,onJoinSuccess)
    }


}