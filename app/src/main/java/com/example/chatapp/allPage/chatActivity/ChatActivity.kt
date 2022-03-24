package com.example.chatapp.allPage.chatActivity

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
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
import com.example.chatapp.util.SmallUtil.getCurrentDateString
import com.example.chatapp.util.SmallUtil.getCurrentTimeStamp
import com.example.chatapp.util.SmallUtil.getCurrentTimeString
import com.example.chatapp.util.SmallUtil.getDialogProgressBarBuilder
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_chat.*


class ChatActivity : AppCompatActivity() {

    companion object {
        const val requestNormalPicturesCode = 998
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
                chatRecyclerviewAdapter.notifyDataSetChanged()
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
                getCurrentDateString(), getCurrentTimeString(), message, ""
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
            //打開相簿
            val openGalleryIntent =
                Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(openGalleryIntent, requestNormalPicturesCode)
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
        if (requestCode == requestNormalPicturesCode) {
            if (resultCode == Activity.RESULT_OK) {
                val imageUir = data?.data
                val imageView = ImageView(this)
                imageView.setImageURI(imageUir)
                AlertDialog.Builder(this)
                    .setTitle("確定要傳送此照片？")
                    .setView(imageView)
                    .setPositiveButton("確定") { _, _ ->
                        val dialogBuilder = getDialogProgressBarBuilder(this).create()
                        dialogBuilder.show()
                        val onMessageSent = object : OnMessageSent {
                            override fun doOnMessageSent() {
                                if (!isDestroyed) {
                                    dialogBuilder.dismiss()
                                }
                            }
                        }
                        val fireRef =
                            FirebaseUtil.mFirebaseStorageInstance.child("channels/$channelUID${getCurrentTimeStamp()}/profile.jpg")
                        fireRef.putFile(imageUir!!).addOnSuccessListener {
                            fireRef.downloadUrl.addOnSuccessListener { uri ->
                                val messageObject = ChannelMessage(
                                    currentUserName,
                                    mFirebaseAuthInstance.currentUser!!.uid,
                                    getCurrentDateString(),
                                    getCurrentTimeString(),
                                    "",
                                    uri.toString()
                                )
                                storeMessageToDB(
                                    channelUID!!,
                                    channelName!!,
                                    messageObject,
                                    onMessageSent,
                                    this
                                )
                            }
                        }
                    }
                    .setNegativeButton("不要") { _, _ -> }
                    .show()
            }
        }
    }

}

interface OnMessageSent {
    fun doOnMessageSent()
}