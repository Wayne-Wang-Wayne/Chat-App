package com.example.chatapp.util

import android.content.Context
import android.util.DisplayMetrics
import android.view.WindowManager

class PhoneSizeUtil(context: Context) {


    val displayMetrics = DisplayMetrics()

    companion object {
        @Volatile
        private var instance: PhoneSizeUtil? = null


        fun getInstance(context: Context): PhoneSizeUtil? {
            if (instance == null) {
                synchronized(PhoneSizeUtil::class.java) {
                    if (instance == null) {
                        instance = PhoneSizeUtil(context.applicationContext)
                    }
                }
            }
            return instance
        }
    }

    init {
        val windowManager: WindowManager =
            (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager)
        windowManager.defaultDisplay.getMetrics(displayMetrics)
    }


    fun getPhoneWidth(): Int {
        return displayMetrics.widthPixels
    }

    fun getPhoneHeight(): Int {
        return displayMetrics.heightPixels
    }
}