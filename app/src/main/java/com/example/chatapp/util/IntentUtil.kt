package com.example.chatapp.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.core.content.ContextCompat.startActivity


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
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

    fun intentToLink(context: Context, url: String) {
        val intent = Intent(Intent.ACTION_VIEW).setData(Uri.parse(url));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent);
    }
}