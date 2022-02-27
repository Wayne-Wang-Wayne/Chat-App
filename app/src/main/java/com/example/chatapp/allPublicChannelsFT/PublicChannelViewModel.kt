package com.example.chatapp.allPublicChannelsFT

import android.app.Application
import androidx.annotation.NonNull
import androidx.lifecycle.MutableLiveData
import com.example.chatapp.allPage.mainActivity.BaseAndroidViewModel
import com.example.chatapp.model.PublicChannels
import com.example.chatapp.model.UserChannels
import com.example.chatapp.util.FirebaseUtil.Companion.PUBLIC_CHANNELS
import com.example.chatapp.util.FirebaseUtil.Companion.USER_CHANNELS
import com.example.chatapp.util.FirebaseUtil.Companion.mFirebaseAuthInstance
import com.example.chatapp.util.FirebaseUtil.Companion.mFirebaseRTDbInstance

class PublicChannelViewModel(@NonNull application: Application) :
    BaseAndroidViewModel(application) {
    private val TAG: String = javaClass.simpleName

    val isError = MutableLiveData<Boolean>()
    val finalPublicChanelLiveData = MutableLiveData<ArrayList<PublicChannels>>()


    fun getPublicChannelsInfo() {
        isLoading.value = true
        mFirebaseRTDbInstance.child(PUBLIC_CHANNELS).get().addOnSuccessListener { snapShot1 ->
            val publicChannels = ArrayList<PublicChannels>()
            for (postSnapShot in snapShot1.children) {
                postSnapShot.getValue(PublicChannels::class.java)
                    ?.let { it1 -> publicChannels.add(it1) }
            }
            mFirebaseRTDbInstance.child(USER_CHANNELS).child(mFirebaseAuthInstance.currentUser!!.uid).get().addOnSuccessListener { snapShot2 ->
                val myChannels = ArrayList<UserChannels>()
                for (postSnapShot in snapShot2.children) {
                    postSnapShot.getValue(UserChannels::class.java)?.let { it2 -> myChannels.add(it2) }
                }
                //剃掉自己已經加入的群組
                val toRemove= ArrayList<PublicChannels>()
                for(publicChannel in publicChannels){
                    for (myChannel in myChannels){
                        if(publicChannel.channelUID.equals(myChannel.channelUID)){
                            toRemove.add(publicChannel)
                        }
                    }
                }
                publicChannels.removeAll(toRemove)
                finalPublicChanelLiveData.value = publicChannels
                isLoading.value = false
            }.addOnFailureListener {
                isLoading.value = false
                isError.value = true
            }

        }.addOnFailureListener {
            isLoading.value = false
            isError.value = true
        }
    }
}