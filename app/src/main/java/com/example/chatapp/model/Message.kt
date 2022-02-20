package com.example.chatapp.model

import androidx.annotation.Keep

@Keep
data class Message(
    var message: String? = "",
    var senderUId: String? = ""

)