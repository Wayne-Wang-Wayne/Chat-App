package com.example.chatapp.util

import android.R.attr
import android.content.Context
import android.os.SystemClock
import android.widget.Toast
import java.util.regex.Matcher
import java.util.regex.Pattern


object SmallUtil {
    var mLastClickTime = 0;
    fun isDoubleClick(): Boolean {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
            return true
        }
        mLastClickTime = SystemClock.elapsedRealtime().toInt()
        return false
    }
    fun quickToast(context:Context,message:String){
        Toast.makeText(context,message,Toast.LENGTH_SHORT).show()
    }
    fun isValidPassword(password:String): Boolean {
        val matcher: Matcher
        val pattern: Pattern = Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=\\S+$).{6,}$")
        matcher = pattern.matcher(password)

        return matcher.matches()
    }
}