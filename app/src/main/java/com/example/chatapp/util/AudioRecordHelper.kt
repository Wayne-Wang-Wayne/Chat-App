package com.example.chatapp.util

import android.content.Context
import android.media.MediaRecorder
import android.net.Uri
import android.os.Handler
import android.widget.Toast
import com.example.chatapp.allPage.chatActivity.ChatActivity
import com.example.chatapp.model.ChannelMessage
import io.opencensus.trace.export.SpanExporter
import java.io.File
import java.io.IOException

class AudioRecordHelper(
    val mContext: Context, private val channelName: String?, private val channelUID: String?,
    private val onMessageSent: ChatActivity.OnMessageSent
) {

    private var recorder: MediaRecorder? = null
    private var fileName: String? =
        "${mContext?.externalCacheDir?.absolutePath}/audiorecordtest.3gp"
    private var timeIsEnough = false
    private var recordIsValid = false
    private val runnable: Runnable = Runnable { timeIsEnough = true }
    private val handler = Handler()

    fun startRecording() {
        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setOutputFile(fileName)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

            try {
                SmallUtil.vibrateDevice(mContext)
                prepare()
                start()
                timeIsEnough = false
                recordIsValid = true
                handler.postDelayed(runnable, 1000)
            } catch (e: IOException) {
                SmallUtil.quickToast(mContext, "錄音異常！")
            }
        }
    }

    fun stopRecording() {

        if (timeIsEnough && recordIsValid) {
            cleanRecorder()
            val newAudioUri = Uri.fromFile(File(fileName!!))
            val audioStorePath =
                "channels/${FirebaseUtil.mFirebaseAuthInstance.currentUser?.uid}/Audio/${SmallUtil.getCurrentTimeStamp()}/chatAudio.3gp"
            val fireRef =
                FirebaseUtil.mFirebaseStorageInstance.child(audioStorePath)
            fireRef.putFile(newAudioUri).addOnSuccessListener {
                val localFile = File.createTempFile("audio", "3gp")
                val messageObject = ChannelMessage(
                    FirebaseUtil.currentUserName,
                    FirebaseUtil.mFirebaseAuthInstance.currentUser!!.uid,
                    SmallUtil.getCurrentDateString(),
                    SmallUtil.getCurrentTimeString(),
                    "",
                    "",
                    "",
                    audioStorePath
                )
                FirebaseUtil.storeMessageToDB(
                    channelUID!!,
                    channelName!!,
                    messageObject,
                    onMessageSent,
                    mContext
                )


            }.addOnFailureListener {
                Toast.makeText(mContext, "傳送音檔失敗", Toast.LENGTH_SHORT).show()
            }
        } else {
            cleanRecorder()
            SmallUtil.quickToast(mContext, "錄音時間過短！")
            ChatActivity.voiceRecordEnable = true
        }
    }

    private fun cleanRecorder() {
        recorder?.apply {
            stop()
            release()
        }
        recorder = null
        timeIsEnough = false
        recordIsValid = false
        handler.removeCallbacks(runnable)
    }
}