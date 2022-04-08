package com.example.chatapp.util

import android.app.AlertDialog
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.chatapp.R
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern


object SmallUtil {
    var mLastClickTime = 0;
    fun isDoubleClick(): Boolean {
        if (System.currentTimeMillis() - mLastClickTime < 1000) {
            return true
        }
        mLastClickTime = System.currentTimeMillis().toInt()
        return false
    }

    fun quickToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun isValidPassword(password: String): Boolean {
        val matcher: Matcher
        val pattern: Pattern = Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=\\S+$).{6,}$")
        matcher = pattern.matcher(password)

        return matcher.matches()
    }

    fun isValidChannelUid(channelUid: String): Boolean {
        val matcher: Matcher
        val pattern: Pattern = Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=\\S+$).{4,}$")
        matcher = pattern.matcher(channelUid)

        return matcher.matches()
    }

    fun simpleDialogUtilWithY(mContext: Context, title: String, message: String) {
        AlertDialog.Builder(mContext)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("好") { _, _ -> }
            .show()
    }

    fun getCurrentTimeStamp(): Int {
        return (System.currentTimeMillis() / 1000).toInt()
    }

    fun getCurrentDateString(): String {
        return SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())
    }

    fun getCurrentTimeString(): String {
        return SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
    }

    fun getCharCount(str: String): Int {
        var count = 0
        for (element in str) {
            count++
        }
        return count
    }

    fun glideProfileUtil(context: Context, width: Int, uri: Uri, imageView: ImageView) {
            Glide.with(context)
                .load(uri)
                .placeholder(R.drawable.default_user_image)
                .error(R.drawable.default_user_image)
                .override(width, width)
                .centerCrop()
                .dontAnimate()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imageView)
    }

    fun glideNormalUtil(context: Context, uri: Uri, imageView: ImageView) {
        Glide.with(context)
            .load(uri)
            .placeholder(R.drawable.place_holder_default_picture)
            .error(R.drawable.chat_image_error)
            .centerCrop()
            .dontAnimate()
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(imageView)
    }

    fun glideNoCutPicture(context: Context, uri: Uri, imageView: ImageView) {
        Glide.with(context)
            .load(uri)
            .placeholder(R.drawable.place_holder_default_picture)
            .error(R.drawable.chat_image_error)
            .dontAnimate()
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(imageView)
    }

    fun getDialogProgressBarBuilder(mContext: Context,title:String): AlertDialog.Builder {
        var progressDialog: AlertDialog
        var builder: AlertDialog.Builder = AlertDialog.Builder(mContext)
        builder.setTitle(title)
        val progressBar = ProgressBar(mContext)
        val lp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        progressBar.layoutParams = lp
        builder.setView(progressBar)
        builder.setCancelable(false)
        return builder
    }

    fun glideFormOnlineVideo(mContext: Context,uri:String,imageView: ImageView){
        val requestOptions = RequestOptions()
        Glide.with(mContext)
            .load(uri)
            .apply(requestOptions)
            .error(R.drawable.chat_image_error)
            .thumbnail(Glide.with(mContext).load(uri))
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(imageView)
    }

    // Vibrates the device for 100 milliseconds.
    fun vibrateDevice(context: Context) {
        val vibrator = getSystemService(context, Vibrator::class.java)
        vibrator?.let {
            if (Build.VERSION.SDK_INT >= 26) {
                it.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                it.vibrate(100)
            }
        }
    }
}