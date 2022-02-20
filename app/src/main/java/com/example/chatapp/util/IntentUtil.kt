package com.example.chatapp.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle

object IntentUtil {

    fun intentToAnyClass(
        context: Context,
        bundle: Bundle? = null,
        cls: Class<*>,
        uri: Uri? = null
    ) {
        run {
            val intent = Intent()
            if (bundle != null) {
                intent.putExtras(bundle)
            }
            if (uri != null) {
                intent.data = uri
            }
            intent.setClass(context, cls)
            context.startActivity(intent)
        }
    }
}