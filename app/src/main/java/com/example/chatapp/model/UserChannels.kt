package com.example.chatapp.model

import androidx.annotation.Keep

@Keep
data class UserChannels(
    var channelUID: String? = "",
    var channelsName: String? = "",
    var timeStamp: Int? = 0,
    var updateTime: String? = "",
    var updateDate: String? = "",
    var lastMessage: String? = "",
    var lastImageUriSent: String? = "",
    var lastVideoUriSent: String? = "",
    var lastVoiceUriSent: String? = "",
    var lastSenderName: String? = "",
    var lastSenderUid: String? = "",
    var needNewTag: Boolean? = false
)