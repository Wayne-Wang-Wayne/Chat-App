package com.example.chatapp.model

import androidx.annotation.Keep

@Keep
data class UserChannels(
    var channelUID: String? = "",
    var channelsName: String? = "",
    var joinOrCreateTime: Int? = 0
)