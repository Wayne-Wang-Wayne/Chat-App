package com.example.chatapp.model

import androidx.annotation.Keep

@Keep
data class User(
    var name: String? = "",
    var email: String? = "",
    var uid: String? = ""
)



