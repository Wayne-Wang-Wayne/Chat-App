package com.example.chatapp.allPage.chatActivity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatapp.R
import com.example.chatapp.customStuff.SafeClickListener.Companion.setSafeOnClickListener
import com.example.chatapp.model.ChannelMessage
import com.example.chatapp.recyclerviewAdapter.ChatRecyclerviewAdapter
import com.example.chatapp.util.FirebaseUtil
import com.example.chatapp.util.FirebaseUtil.Companion.CHATS
import com.example.chatapp.util.FirebaseUtil.Companion.MESSAGE
import com.example.chatapp.util.FirebaseUtil.Companion.currentUserName
import com.example.chatapp.util.FirebaseUtil.Companion.listenToRTDBForMessage
import com.example.chatapp.util.FirebaseUtil.Companion.mFirebaseAuthInstance
import com.example.chatapp.util.FirebaseUtil.Companion.storeMessageToDB
import com.example.chatapp.util.SmallUtil
import com.example.chatapp.util.SmallUtil.getCurrentDateString
import com.example.chatapp.util.SmallUtil.getCurrentTimeString
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.activity_chat.chatRecyclerview

class ChatActivity : AppCompatActivity() {

    private lateinit var chatRecyclerviewAdapter: ChatRecyclerviewAdapter
    private lateinit var messageList: ArrayList<ChannelMessage>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        chatRecyclerviewAdapter = ChatRecyclerviewAdapter(this, messageList)
        chatRecyclerview.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = chatRecyclerviewAdapter
        }
        setView()

    }

    private fun setView() {
        initToolBar()
        val channelName = intent.extras?.getString("channelName")
        val channelUID = intent.extras?.getString("channelUID")
        chat_activity_title.text = channelName

        //logic for adding data to recyclerview
        listenToRTDBForMessage(channelUID!!, object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                messageList.clear()
                for (postSnapShot in snapshot.children) {
                    val message = postSnapShot.getValue(ChannelMessage::class.java)
                    messageList.add(message!!)
                }
                chatRecyclerviewAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

        sendButton.setSafeOnClickListener {
            val message = messageBox.text.toString()
            val messageObject = ChannelMessage(
                currentUserName, mFirebaseAuthInstance.currentUser!!.uid,
                getCurrentDateString(), getCurrentTimeString(), message
            )
            val onMessageSent = object :OnMessageSent{
                override fun doOnMessageSent() {
                    messageBox.setText("")
                }
            }
            storeMessageToDB(channelUID, channelName!!, messageObject,onMessageSent)

        }
    }

    private fun initToolBar() {
        setSupportActionBar(chat_activity_toolbar)
    }

}

interface OnMessageSent{
    fun doOnMessageSent()
}