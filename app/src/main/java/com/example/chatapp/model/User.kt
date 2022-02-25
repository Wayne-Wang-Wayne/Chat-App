package com.example.chatapp.model

import androidx.annotation.Keep

@Keep
data class User(
    var email: String? = "",
    var name: String? = "",
    var userUID: String? = "",
    var userPhotoUrl: String? = ""
)



