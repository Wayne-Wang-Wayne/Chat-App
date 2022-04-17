package com.example.chatapp.allPage.chatActivity

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.view.*
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.abedelazizshe.lightcompressorlibrary.CompressionListener
import com.abedelazizshe.lightcompressorlibrary.VideoCompressor
import com.abedelazizshe.lightcompressorlibrary.VideoQuality
import com.abedelazizshe.lightcompressorlibrary.config.Configuration
import com.example.chatapp.R
import com.example.chatapp.customStuff.SafeClickListener.Companion.setSafeOnClickListener
import com.example.chatapp.model.ChannelMessage
import com.example.chatapp.recyclerviewAdapter.ChatRecyclerviewAdapter
import com.example.chatapp.util.AudioPlayHelper.Companion.stopAllAudioPlayer
import com.example.chatapp.util.AudioRecordHelper
import com.example.chatapp.util.FirebaseUtil
import com.example.chatapp.util.FirebaseUtil.Companion.USER_CHANNELS
import com.example.chatapp.util.FirebaseUtil.Companion.currentUserName
import com.example.chatapp.util.FirebaseUtil.Companion.listenToRTDBForMessage
import com.example.chatapp.util.FirebaseUtil.Companion.mFirebaseAuthInstance
import com.example.chatapp.util.FirebaseUtil.Companion.mFirebaseRTDbInstance
import com.example.chatapp.util.FirebaseUtil.Companion.storeMessageToDB
import com.example.chatapp.util.SmallUtil
import com.example.chatapp.util.SmallUtil.getCurrentDateString
import com.example.chatapp.util.SmallUtil.getCurrentTimeStamp
import com.example.chatapp.util.SmallUtil.getCurrentTimeString
import com.example.chatapp.util.SmallUtil.getDialogProgressBarBuilder
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_chat.*
import java.io.File


class ChatActivity : AppCompatActivity() {

    companion object {
        const val requestPicturesOrVideoCode = 998
        var sharedByOtherAppText: String? = null
        var voiceRecordEnable = true
    }

    private lateinit var chatRecyclerviewAdapter: ChatRecyclerviewAdapter
    private lateinit var messageList: ArrayList<ChannelMessage>
    private var mMenu: Menu? = null
    private var channelName: String? = null
    private var channelUID: String? = null
    private var player: MediaPlayer? = null
    private var recorder: MediaRecorder? = null
    private var fileName: String? = null
    private var requestPermissionLauncher: ActivityResultLauncher<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStatusBarColor()
        registerPermissionLauncher()
        fileName = "${externalCacheDir?.absolutePath}/audiorecordtest.3gp"
        setContentView(R.layout.activity_chat)
        messageList = ArrayList<ChannelMessage>()
        chatRecyclerviewAdapter = ChatRecyclerviewAdapter(applicationContext, messageList, this)
        chatRecyclerview.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = chatRecyclerviewAdapter
        }
        setView()
        senseIfAtBottomAnimation()
        handleInfoFromOtherApp()
    }

    override fun onPause() {
        super.onPause()
        //離開時設置new tag不顯示
        //如果是退出群組process就不需要動，因為資料刪掉了
        mFirebaseRTDbInstance.child(USER_CHANNELS)
            .child(mFirebaseAuthInstance.currentUser?.uid!!)
            .child(intent.extras?.getString("channelUID")!!).get()
            .addOnSuccessListener { snapShot ->
                if (snapShot.value != null) {
                    mFirebaseRTDbInstance.child(USER_CHANNELS)
                        .child(mFirebaseAuthInstance.currentUser?.uid!!)
                        .child(intent.extras?.getString("channelUID")!!).child("needNewTag")
                        .setValue(false)
                }
            }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setView() {
        initToolBar()
        channelName = intent.extras?.getString("channelName")
        channelUID = intent.extras?.getString("channelUID")
        chat_activity_title.text = channelName
        chat_back_press.setSafeOnClickListener { finish() }

        messageBox.addTextChangedListener {
            if (it.toString() == "") {
                sendMessageButton.visibility = View.GONE
                voiceRecordSmallButton.visibility = View.VISIBLE
            } else {
                sendMessageButton.visibility = View.VISIBLE
                voiceRecordSmallButton.visibility = View.GONE
            }
        }

        //logic for adding data to recyclerview
        listenToRTDBForMessage(channelUID!!, object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                messageList.clear()
                for (postSnapShot in snapshot.children) {
                    val message = postSnapShot.getValue(ChannelMessage::class.java)
                    messageList.add(message!!)
                }
                chatRecyclerviewAdapter.notifyItemRangeInserted(messageList.size - 1, 1)
                if (messageList.size != 0) {
                    arrowDisAppearAnimation()
                    chatRecyclerview.scrollToPosition(messageList.size - 1)
                }

            }

            override fun onCancelled(error: DatabaseError) {

            }


        })

        sendMessageButton.setSafeOnClickListener {
            val message = messageBox.text.toString()
            sendTextMessage(message)
        }

        sendImageButton.setSafeOnClickListener {
            //打開相簿選擇影片或相片
            openGallery()

        }
        chat_activity_arrow_down.setSafeOnClickListener {
            arrowDisAppearAnimation()
            chatRecyclerview.smoothScrollToPosition(messageList.size - 1)
        }

        val audioRecordHelper =
            AudioRecordHelper(this, channelName, channelUID, object : OnMessageSent {
                override fun doOnMessageSent() {
                    Toast.makeText(this@ChatActivity, "傳送語音訊息成功", Toast.LENGTH_SHORT).show()
                }
            })
        voiceRecordSmallButton.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.RECORD_AUDIO
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        if (voiceRecordEnable) {
                            audioRecordHelper.startRecording()
                            record_voice_animation.visibility = View.VISIBLE
                        }
                    } else {
                        askAudioPermission()
                    }
                    return@setOnTouchListener true
                }
                MotionEvent.ACTION_UP -> {

                    if (ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.RECORD_AUDIO
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        if (voiceRecordEnable) {
                            voiceRecordEnable = false
                            audioRecordHelper.stopRecording()
                            record_voice_animation.visibility = View.GONE
                        }
                    } else {
                        askAudioPermission()
                    }
                    return@setOnTouchListener true
                }

            }
            return@setOnTouchListener true

        }

    }

    private fun initToolBar() {
        setSupportActionBar(chat_activity_toolbar)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menu?.clear()
        mMenu = menu
        menuInflater.inflate(R.menu.menu_chat_activity, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.leaveChannel) {
            //logic for log out
            FirebaseUtil.leaveChannel(this, intent.extras?.getString("channelUID")!!)
            return true
        }
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == requestPicturesOrVideoCode) {
            if (resultCode == Activity.RESULT_OK) {
                val fileUir = data?.data

                // 如果檔案太大擋下來並return
                inspectFile(fileUir!!)
            }
        }
    }

    private fun openGallery() {
        val pickIntent =
            Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickIntent.type = "image/* video/*"
        startActivityForResult(pickIntent, requestPicturesOrVideoCode)
    }

    private fun imageHandle(imageUir: Uri) {
        val imageView = ImageView(this)
        imageView.setImageURI(imageUir)
        AlertDialog.Builder(this)
            .setTitle("確定要傳送此照片？")
            .setView(imageView)
            .setPositiveButton("確定") { _, _ ->
                val dialogBuilder = getDialogProgressBarBuilder(this, "照片傳送中...").create()
                dialogBuilder.show()
                val onMessageSent = object : OnMessageSent {
                    override fun doOnMessageSent() {
                        if (!isDestroyed) {
                            dialogBuilder.dismiss()
                        }
                    }
                }
                val fireRef =
                    FirebaseUtil.mFirebaseStorageInstance.child("channels/$channelUID/Image/${getCurrentTimeStamp()}/chatImage.jpg")
                fireRef.putFile(imageUir).addOnSuccessListener {
                    fireRef.downloadUrl.addOnSuccessListener { uri ->
                        val messageObject = ChannelMessage(
                            currentUserName,
                            mFirebaseAuthInstance.currentUser!!.uid,
                            getCurrentDateString(),
                            getCurrentTimeString(),
                            "",
                            uri.toString(),
                            "",
                            ""
                        )
                        storeMessageToDB(
                            channelUID!!,
                            channelName!!,
                            messageObject,
                            onMessageSent,
                            this
                        )
                    }
                }.addOnProgressListener {
                    val progress = (100.0 * it.bytesTransferred) / it.totalByteCount
                    dialogBuilder.setTitle("照片傳送中...${progress.toInt()} %")
                }.addOnFailureListener {
                    dialogBuilder.dismiss()
                    SmallUtil.quickToast(this, "照片傳送失敗！請檢查網路！")
                }
            }
            .setNegativeButton("不要") { _, _ -> }
            .show()
    }

    private fun inspectFile(fileUir: Uri) {
        //檢查檔案大小，太大直接擋掉
        if (getFileSize(fileUir) > 40000000) {
            SmallUtil.simpleDialogUtilWithY(this, "錯誤", "檔案太大，無法傳送(小於40MB)！")
            return
        } else {
            if (fileUir.toString().contains("image")) {
                //handle image
                imageHandle(fileUir)
            } else if (fileUir.toString().contains("video")) {
                //handle video
                videoHandle(fileUir)
            }
        }


    }

    private fun deleteVideoFile(selectedFilePath: String) {
        val file = File(selectedFilePath)
        val deleted = file.delete()
    }

    private fun videoHandle(fileUir: Uri) {

        //先擋alert dialog確認後再繼續
        AlertDialog.Builder(this)
            .setTitle("確定要傳送此影片？")
            .setPositiveButton("確定") { _, _ ->
                val dialogBuilder = getDialogProgressBarBuilder(this, "影片壓縮中...").create()
                dialogBuilder.show()
                val uris = ArrayList<Uri>()
                uris.add(fileUir)
                VideoCompressor.start(
                    context = applicationContext, // => This is required
                    uris = uris, // => Source can be provided as content uris
                    isStreamable = true,
                    saveAt = Environment.DIRECTORY_MOVIES, // => the directory to save the compressed video(s)
                    listener = object : CompressionListener {
                        override fun onProgress(index: Int, percent: Float) {
                            // handle while compressing 進度
                            runOnUiThread {
                                dialogBuilder.setTitle("影片壓縮中...${percent.toInt()}%")
                            }
                        }

                        override fun onStart(index: Int) {
                            // Compression start
                        }

                        override fun onSuccess(index: Int, size: Long, path: String?) {
                            // handle upload to firebase
                            dialogBuilder.setTitle("影片上傳中...")

                            val newVideoUri = Uri.fromFile(File(path!!))
                            val fireRef =
                                FirebaseUtil.mFirebaseStorageInstance.child("channels/$channelUID/Video/${getCurrentTimeStamp()}/chatVideo.mp4")
                            fireRef.putFile(newVideoUri).addOnSuccessListener {
                                deleteVideoFile(path)
                                fireRef.downloadUrl.addOnSuccessListener { uri ->
                                    val messageObject = ChannelMessage(
                                        currentUserName,
                                        mFirebaseAuthInstance.currentUser!!.uid,
                                        getCurrentDateString(),
                                        getCurrentTimeString(),
                                        "",
                                        "",
                                        uri.toString(),
                                        ""
                                    )
                                    val onMessageSent = object : OnMessageSent {
                                        override fun doOnMessageSent() {
                                            if (!isDestroyed) {
                                                dialogBuilder.dismiss()
                                            }
                                        }
                                    }
                                    storeMessageToDB(
                                        channelUID!!,
                                        channelName!!,
                                        messageObject,
                                        onMessageSent,
                                        this@ChatActivity
                                    )
                                }

                            }.addOnFailureListener {
                                deleteVideoFile(path!!)
                                dialogBuilder.dismiss()
                                SmallUtil.quickToast(this@ChatActivity, "影片傳送失敗！請檢查網路！")
                            }.addOnProgressListener {
                                val progress =
                                    (100.0 * it.bytesTransferred) / it.totalByteCount
                                dialogBuilder.setTitle("影片傳送中...${progress.toInt()} %")
                            }.addOnCanceledListener {
                                deleteVideoFile(path!!)
                                dialogBuilder.dismiss()
                                SmallUtil.quickToast(this@ChatActivity, "影片傳送失敗！")
                            }

                        }

                        override fun onFailure(index: Int, failureMessage: String) {
                            // On Failure
                            dialogBuilder.dismiss()
                            SmallUtil.quickToast(this@ChatActivity, "影片處理失敗！")
                        }

                        override fun onCancelled(index: Int) {
                            // On Cancelled
                        }

                    },
                    configureWith = Configuration(
                        quality = VideoQuality.LOW,
                        frameRate = 0, /*Int, ignore, or null*/
                        isMinBitrateCheckEnabled = false,
                        videoBitrate = 0, /*Int, ignore, or null*/
                        disableAudio = false, /*Boolean, or ignore*/
                        keepOriginalResolution = false, /*Boolean, or ignore*/
                    )
                )
            }.setNegativeButton("不要") { _, _ ->
            }.show()
    }

    private fun getFileSize(fileUir: Uri): Long {
        val cursor: Cursor? = contentResolver.query(
            fileUir,
            null, null, null, null
        )
        cursor?.moveToFirst()
        val size: Long =
            cursor!!.getLong(cursor.getColumnIndexOrThrow(OpenableColumns.SIZE))
        cursor.close()
        return size
    }

    private fun handleInfoFromOtherApp() {
        if (sharedByOtherAppText != null) {
            sendTextMessage(sharedByOtherAppText!!)
            sharedByOtherAppText = null
        }
    }

    private fun sendTextMessage(message: String) {
        val messageObject = ChannelMessage(
            currentUserName, mFirebaseAuthInstance.currentUser!!.uid,
            getCurrentDateString(), getCurrentTimeString(), message, "", "", ""
        )
        val onMessageSent = object : OnMessageSent {
            override fun doOnMessageSent() {
                if (!isDestroyed) {
                    messageBox.setText("")
                }
            }
        }
        storeMessageToDB(channelUID!!, channelName!!, messageObject, onMessageSent, this)
    }

    private fun checkAudioPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        ) {

        }
    }

    private fun askAudioPermission() {
        requestPermissionLauncher?.launch(
            Manifest.permission.RECORD_AUDIO
        )
    }

    private fun registerPermissionLauncher() {
        requestPermissionLauncher =
            registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                if (isGranted) {
                } else {
                }
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopAllAudioPlayer()
    }

    interface OnMessageSent {
        fun doOnMessageSent()
    }

    private fun setStatusBarColor() {
        val window: Window = window
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(this, R.color.tool_bar_background)
    }

    private fun senseIfAtBottomAnimation() {
        chatRecyclerview.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (!recyclerView.canScrollVertically(1)) {
                    //如果已經在就不用再set
                    arrowDisAppearAnimation()
                } else {
                    //如果已經在就不用再set
                    arrowAppearAnimation()
                }
            }
        })
    }

    private fun arrowDisAppearAnimation() {
        if (chat_activity_arrow_down.visibility == View.VISIBLE) {
            chat_activity_arrow_down.animate()
                .translationY(144f)
                .alpha(0.0f)
                .setDuration(300)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?) {
                        super.onAnimationEnd(animation)
                        chat_activity_arrow_down.visibility = View.GONE
                    }
                })
        }
    }

    private fun arrowAppearAnimation() {
        if (chat_activity_arrow_down.visibility == View.GONE) {
            chat_activity_arrow_down.visibility = View.VISIBLE
            chat_activity_arrow_down.alpha = 0f
            if (chat_activity_arrow_down.y == 0f) {
                chat_activity_arrow_down.y = 144f
            }
            chat_activity_arrow_down.animate()
                .translationY(0f)
                .alpha(1.0f)
                .setDuration(300)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?) {
                        super.onAnimationEnd(animation)

                    }
                })
        }
    }
}