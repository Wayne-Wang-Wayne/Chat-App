package com.example.chatapp.model

import androidx.annotation.Keep

@Keep
data class PublicChannels(
    var channelUID: String? = "",
    var channelName: String? = "",
    var userAmount: Int? = 1
)