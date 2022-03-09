package com.example.chatapp.util

import android.content.Context
import android.util.Log
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.json.JSONException
import org.json.JSONObject

class FirebaseMessageService : FirebaseMessagingService() {


    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {


    }


    fun subscribeToMultipleTopic(topicList:ArrayList<String>){
        for(topic in topicList){
            FirebaseMessaging.getInstance().subscribeToTopic(topic)
        }
    }

    fun sendFirebaseMessageWithVolley(mContext: Context, topic:String, title:String, body:String) {
        val mRequestQue: RequestQueue = Volley.newRequestQueue(mContext)

        val json = JSONObject()
        try {
            json.put("to", "/topics/$topic")
            val notificationObj = JSONObject()
            notificationObj.put("title", title)
            notificationObj.put("body", body)
            //replace notification with data when went send data
            json.put("notification", notificationObj)
            val url = "https://fcm.googleapis.com/fcm/send"
            val request: JsonObjectRequest = object : JsonObjectRequest(
                Method.POST, url,
                json,
                { Log.d("MUR", "onResponse: ") },
                { error -> Log.d("MUR", "onError: " + error.networkResponse) }
            ) {
                override fun getHeaders(): MutableMap<String, String> {
                    val header: MutableMap<String, String> = HashMap()
                    header["content-type"] = "application/json"
                    header["authorization"] = "key=AAAAbWO0CmY:APA91bHgtX0QmkUGcZWVXIspiz0M0H136VNN8EVU6-Zx34cf40Al-6L5eVezInvnPoP8lgQoHncTpvitbamGYwM2-FMsppy9bkf-ex52WDIQCRungv00g4njhjT5IEXdptS2j5F1hy2w"
                    return header
                }
            }
            mRequestQue.add(request)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }
}