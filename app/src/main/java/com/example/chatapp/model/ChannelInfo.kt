package com.example.chatapp.model

import androidx.annotation.Keep

@Keep
data class ChannelInfo(
    var lastMessageSent: String? = "",
    var channelName: String? = "",
    var isPublic: Boolean? = null
)

@Keep
data class OnlyUserUid(
    var userUID: String? = ""
)