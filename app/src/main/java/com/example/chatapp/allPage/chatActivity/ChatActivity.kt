package com.example.chatapp.chatActivity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatapp.R
import com.example.chatapp.customStuff.SafeClickListener.Companion.setSafeOnClickListener
import com.example.chatapp.model.Message
import com.example.chatapp.recyclerviewAdapter.ChatRecyclerviewAdapter
import com.example.chatapp.util.FirebaseUtil.Companion.CHATS
import com.example.chatapp.util.FirebaseUtil.Companion.MESSAGE
import com.example.chatapp.util.FirebaseUtil.Companion.listenToRTDBForMessage
import com.example.chatapp.util.FirebaseUtil.Companion.mFirebaseAuthInstance
import com.example.chatapp.util.FirebaseUtil.Companion.mFirebaseRTDbInstance
import com.example.chatapp.util.FirebaseUtil.Companion.storeMessageToDB
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.activity_chat.chatRecyclerview

class ChatActivity : AppCompatActivity() {

    private lateinit var chatRecyclerviewAdapter: ChatRecyclerviewAdapter
    private lateinit var messageList: ArrayList<Message>
    var receiverRoomId: String? = null
    var senderRoomId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        messageList = ArrayList()
        chatRecyclerviewAdapter = ChatRecyclerviewAdapter(this, messageList)
        chatRecyclerview.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = chatRecyclerviewAdapter
        }
        setView()

    }

    private fun setView() {
        initToolBar()
        val friendName = intent.extras?.getString("friendName")
        val friendUid = intent.extras?.getString("friendUid")
        chat_activity_title.text = friendName
        val myUid = mFirebaseAuthInstance.currentUser?.uid
        senderRoomId = friendUid + myUid
        receiverRoomId = myUid + friendUid

        //logic for adding data to recyclerview
        listenToRTDBForMessage(CHATS, senderRoomId!!, MESSAGE, object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                messageList.clear()
                for (postSnapShot in snapshot.children) {
                    val message = postSnapShot.getValue(Message::class.java)
                    messageList.add(message!!)
                }
                chatRecyclerviewAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

        sendButton.setSafeOnClickListener {
            val message = messageBox.text.toString()
            val messageObject = Message(message, myUid)
            storeMessageToDB(senderRoomId!!, receiverRoomId!!, messageObject)
            messageBox.setText("")
        }
    }

    private fun initToolBar() {
        setSupportActionBar(chat_activity_toolbar)
    }

}