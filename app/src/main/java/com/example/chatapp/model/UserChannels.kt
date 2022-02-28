package com.example.chatapp.model

import androidx.annotation.Keep

@Keep
data class UserChannels(
    var channelUID: String? = "",
    var channelsName: String? = "",
    var timeStamp: Int? = 0,
    var updateTime:String? = "",
    var updateDate:String? = "",
    var lastMessage:String? = "",
    var needNewTag:Boolean? = false
)