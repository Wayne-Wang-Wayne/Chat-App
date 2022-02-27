package com.example.chatapp.allPublicChannelsFT

import android.app.Application
import androidx.annotation.NonNull
import androidx.lifecycle.MutableLiveData
import com.example.chatapp.allPage.mainActivity.BaseAndroidViewModel
import com.example.chatapp.allPage.mainActivity.BaseTabsModel
import com.example.chatapp.model.PublicChannels
import com.example.chatapp.model.UserChannels
import com.example.chatapp.util.FirebaseUtil.Companion.PUBLIC_CHANNELS
import com.example.chatapp.util.FirebaseUtil.Companion.USER_CHANNELS
import com.example.chatapp.util.FirebaseUtil.Companion.mFirebaseAuthInstance
import com.example.chatapp.util.FirebaseUtil.Companion.mFirebaseRTDbInstance

class PublicChannelViewModel(@NonNull application: Application) :
    BaseAndroidViewModel(application) {
    private val TAG: String = javaClass.simpleName
    private val loadingStatus = MutableLiveData<Boolean>()
    val isError = MutableLiveData<Boolean>()
    val publicChanelLiveData = MutableLiveData<ArrayList<PublicChannels>>()
    val myChanelLiveData = MutableLiveData<ArrayList<UserChannels>>()

    fun getPublicChannelsInfo() {
        loadingStatus.value = true
        mFirebaseRTDbInstance.child(PUBLIC_CHANNELS).get().addOnSuccessListener { snapShot1 ->
            val publicChannels = ArrayList<PublicChannels>()
            for (postSnapShot in snapShot1.children) {
                postSnapShot.getValue(PublicChannels::class.java)
                    ?.let { it1 -> publicChannels.add(it1) }
            }
            publicChanelLiveData.value = publicChannels
            mFirebaseRTDbInstance.child(USER_CHANNELS).child(mFirebaseAuthInstance.currentUser!!.uid).get().addOnSuccessListener { snapShot2 ->
                val myChannels = ArrayList<UserChannels>()
                for (postSnapShot in snapShot2.children) {
                    postSnapShot.getValue(UserChannels::class.java)?.let { it2 -> myChannels.add(it2) }
                }
                myChanelLiveData.value = myChannels
                loadingStatus.value = false
            }.addOnFailureListener {
                loadingStatus.value = false
                isError.value = true
            }

        }.addOnFailureListener {
            loadingStatus.value = false
            isError.value = true
        }
    }
}