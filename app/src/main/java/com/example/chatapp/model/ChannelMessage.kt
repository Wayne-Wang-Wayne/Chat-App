package com.example.chatapp.model

import androidx.annotation.Keep

@Keep
data class ChannelMessage(
    var senderName: String? = "",
    var sentUserUID: String? = "",
    var messageDate: String? = "",
    var messageTime: String? = "",
    var message: String? = "",
    var imageUri:String? = ""

)