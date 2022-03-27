package com.example.chatapp.allPage.chatActivity

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.abedelazizshe.lightcompressorlibrary.CompressionListener
import com.abedelazizshe.lightcompressorlibrary.VideoCompressor
import com.abedelazizshe.lightcompressorlibrary.VideoQuality
import com.abedelazizshe.lightcompressorlibrary.config.Configuration
import com.example.chatapp.R
import com.example.chatapp.customStuff.SafeClickListener.Companion.setSafeOnClickListener
import com.example.chatapp.model.ChannelMessage
import com.example.chatapp.recyclerviewAdapter.ChatRecyclerviewAdapter
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
    }

    private lateinit var chatRecyclerviewAdapter: ChatRecyclerviewAdapter
    private lateinit var messageList: ArrayList<ChannelMessage>
    private var mMenu: Menu? = null
    private var channelName: String? = null
    private var channelUID: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        messageList = ArrayList<ChannelMessage>()
        chatRecyclerviewAdapter = ChatRecyclerviewAdapter(applicationContext, messageList)
        chatRecyclerview.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = chatRecyclerviewAdapter
        }
        setView()

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

    private fun setView() {
        initToolBar()
        channelName = intent.extras?.getString("channelName")
        channelUID = intent.extras?.getString("channelUID")
        chat_activity_title.text = channelName
        chat_back_press.setSafeOnClickListener { finish() }

        //logic for adding data to recyclerview
        listenToRTDBForMessage(channelUID!!, object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                messageList.clear()
                for (postSnapShot in snapshot.children) {
                    val message = postSnapShot.getValue(ChannelMessage::class.java)
                    messageList.add(message!!)
                }
                chatRecyclerviewAdapter.notifyItemRangeInserted(messageList.size-1,1)
                if (messageList.size != 0) {
                    chatRecyclerview.scrollToPosition(messageList.size - 1)
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

        sendMessageButton.setSafeOnClickListener {
            val message = messageBox.text.toString()
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

        sendImageButton.setSafeOnClickListener {
            //打開相簿選擇影片或相片
            openGallery()

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

                //todo 如果檔案太大擋下來並return
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


}

interface OnMessageSent {
    fun doOnMessageSent()
}