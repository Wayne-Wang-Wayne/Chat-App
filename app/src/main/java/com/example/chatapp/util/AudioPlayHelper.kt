package com.example.chatapp.util

import android.app.Activity
import android.content.Context
import android.media.MediaPlayer
import android.os.Handler
import android.util.Log
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList


class AudioPlayHelper(
    val mActivity: Activity,
    val mContext: Context,
    private val audioPath: String,
    private val audioProgressInterface: AudioProgressInterface
) {

    companion object{
        val audioPlayerList = ArrayList<AudioPlayHelper>()

        fun stopAllAudioPlayer(){
            for (audioPlayer in audioPlayerList){
                audioPlayer.stopPlaying()
            }
        }
    }

    private var player: MediaPlayer? = null

    val timer = Timer()


    fun startPlaying() {
        player = MediaPlayer()

        player?.setOnPreparedListener {
            setProgress(it)
        }

        player!!.apply {
            try {
                val fireRef = FirebaseUtil.mFirebaseStorageInstance.child(audioPath)
                fireRef.getBytes(1024 * 1024 * 5).addOnSuccessListener {

                    try {
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
                    }catch (e:Exception){

                    }

                }


            } catch (e: IOException) {
                Log.e("LOG_TAG", "prepare() failed")
            }
        }
    }

    fun stopPlaying() {
        timer.cancel()
        player?.release()
        player = null
        mActivity.runOnUiThread {
            // call callback
            Handler().postDelayed(Runnable {
                audioProgressInterface.onAudioPlaying(1F)
            },500)
        }
        audioPlayerList.remove(this)
    }

    private fun setProgress(mMediaPlayer: MediaPlayer) {

        val totalTime = mMediaPlayer.duration
        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                try {
                val proportion = mMediaPlayer.currentPosition.toFloat() / totalTime.toFloat()
                mActivity.runOnUiThread {
                    // call callback
                    audioProgressInterface.onAudioPlaying(proportion)
                }
                    if (mMediaPlayer.currentPosition >= totalTime) {
                        stopPlaying()
                    }
                }catch (e:Exception){

                }
            }
        }, 0, 5)
    }


}

interface AudioProgressInterface {
    fun onAudioPlaying(proportion: Float)
}