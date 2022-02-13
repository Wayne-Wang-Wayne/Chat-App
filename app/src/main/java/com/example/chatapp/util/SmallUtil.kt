package com.example.chatapp.util

import android.os.SystemClock

object SmallUtil {
    var mLastClickTime = 0;
    fun isDoubleClick(): Boolean {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
            return true
        }
        mLastClickTime = SystemClock.elapsedRealtime().toInt()
        return false
    }
}