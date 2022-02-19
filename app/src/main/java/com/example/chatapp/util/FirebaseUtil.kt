package com.example.chatapp.util

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

object FirebaseUtil {

    val mAuth = FirebaseAuth.getInstance()
    val mDbRef = FirebaseDatabase.getInstance().reference


}