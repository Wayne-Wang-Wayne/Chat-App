package com.example.chatapp.util

import android.app.Activity
import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.*


class AudioPlayHelper(
    val mActivity: Activity,
    val mContext: Context,
    private val audioPath: String,
    private val audioProgressInterface: AudioProgressInterface
) {

    private var player: MediaPlayer? = null


    fun startPlaying() {
        player = MediaPlayer()

        player?.setOnPreparedListener {
            setProgress(it)
        }

        player!!.apply {
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

    private fun setProgress(mMediaPlayer: MediaPlayer) {

        val totalTime = mMediaPlayer.duration
        val timer = Timer()
        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {

                Log.d("Progresssss", mMediaPlayer.currentPosition.toString())
                Log.d("Progresssss", totalTime.toString())

                val proportion = mMediaPlayer.currentPosition.toFloat() / totalTime.toFloat()
                mActivity.runOnUiThread {
                    // call callback
                    audioProgressInterface.onAudioPlaying(proportion)
                }

                if (mMediaPlayer.currentPosition >= totalTime) {
                    stopPlaying()
                    cancel()
                }
            }
        }, 0, 5)
    }


}

interface AudioProgressInterface {
    fun onAudioPlaying(proportion: Float)
}