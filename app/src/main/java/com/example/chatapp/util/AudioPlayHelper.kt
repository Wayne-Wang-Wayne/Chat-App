package com.example.chatapp.util

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

class AudioPlayHelper(val mContext: Context, val audioPath: String) {

    private var player: MediaPlayer? = null


    fun startPlaying() {
        player = MediaPlayer().apply {
            try {
                val fireRef = FirebaseUtil.mFirebaseStorageInstance.child(audioPath)
                fireRef.getBytes(30000).addOnSuccessListener {

                    val temp3gp = File.createTempFile("wayne", "3gp", mContext?.cacheDir)
                    temp3gp.deleteOnExit()
                    val fos = FileOutputStream(temp3gp)
                    fos.write(it)
                    fos.close()
                    val fis = FileInputStream(temp3gp)
                    reset()
                    setDataSource(fis.fd)
                    prepare()
                    start()
                }


            } catch (e: IOException) {
                Log.e("LOG_TAG", "prepare() failed")
            }
        }
    }

    fun stopPlaying() {
        player?.release()
        player = null
    }
}