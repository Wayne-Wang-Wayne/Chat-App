package com.example.chatapp.recyclerviewAdapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.chatapp.R
import com.example.chatapp.allPage.splash.SplashActivity.Companion.allUserProfileUrl
import com.example.chatapp.model.ChannelMessage
import com.example.chatapp.util.FirebaseUtil
import com.example.chatapp.util.FirebaseUtil.Companion.mFirebaseAuthInstance
import com.example.chatapp.util.SmallUtil
import com.squareup.picasso.Picasso

class ChatRecyclerviewAdapter(
    private val mContext: Context,
    private val messageList: ArrayList<ChannelMessage>
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val colorSettingList = ArrayList<String>()
    val MESSAGE_RECEIVED = 1
    val MESSAGE_SENT = 2


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            MESSAGE_SENT -> {
                SentViewHolder(
                    LayoutInflater.from(mContext).inflate(R.layout.message_sent_box, parent, false)
                )
            }
            MESSAGE_RECEIVED -> {
                ReceivedViewHolder(
                    LayoutInflater.from(mContext)
                        .inflate(R.layout.message_received_box, parent, false)
                )
            }
            else -> {
                //will not go in to this line
                SentViewHolder(
                    LayoutInflater.from(mContext).inflate(R.layout.message_sent_box, parent, false)
                )
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val currentMessage = messageList[position]

        if (holder.javaClass == SentViewHolder::class.java) {
            //do the stuff for sent view holder
            holder as SentViewHolder
            //set sent message
            holder.tvSentMessage.text = currentMessage.message
            //set send time
            holder.tvSentTime.text =
                "${modifyDate(currentMessage.messageDate!!)} ${modifyTime(currentMessage.messageTime!!)}"
        }
        if (holder.javaClass == ReceivedViewHolder::class.java) {
            //do the stuff for received view holder
            holder as ReceivedViewHolder
            //set received message
            holder.tvReceivedMessage.text = currentMessage.message

            setReceiveBoxColor(currentMessage.senderName!!, holder.tvReceivedMessageParent)

            //set profile picture
            if (allUserProfileUrl[currentMessage.sentUserUID] != null) {
                    SmallUtil.glideProfileUtil(mContext, 200, allUserProfileUrl[currentMessage.sentUserUID]!!, holder.iv_myProfilePictureInReceiveBox)
            } else {
                holder.iv_myProfilePictureInReceiveBox.setImageResource(R.drawable.default_user_image)
            }

            //set receiver name and also hide duplicate name
            holder.tvReceiveName.text = currentMessage.senderName
            if (position > 0 && messageList[position].senderName == messageList[position - 1].senderName) {
                holder.tvReceiveName.visibility = View.GONE
                holder.profilePictureGroupInReceiveBox.visibility = View.GONE
            } else {
                holder.tvReceiveName.visibility = View.VISIBLE
                holder.profilePictureGroupInReceiveBox.visibility = View.VISIBLE
            }
            //set receive time
            holder.tvReceiveTime.text =
                "${modifyDate(currentMessage.messageDate!!)} ${modifyTime(currentMessage.messageTime!!)}"
        }
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    override fun getItemViewType(position: Int): Int {
        val currentMessage = messageList[position]
        return if (mFirebaseAuthInstance.currentUser?.uid.equals(currentMessage.sentUserUID)) {
            MESSAGE_SENT
        } else {
            MESSAGE_RECEIVED
        }
    }

    class SentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvSentMessage: TextView = itemView.findViewById<TextView>(R.id.tvSentMessage)
        val tvSentTime: TextView = itemView.findViewById<TextView>(R.id.tvSentTime)
    }

    class ReceivedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvReceiveName: TextView = itemView.findViewById<TextView>(R.id.tvReceiveName)
        val tvReceivedMessage: TextView = itemView.findViewById<TextView>(R.id.tvReceivedMessage)
        val tvReceiveTime: TextView = itemView.findViewById<TextView>(R.id.tvReceiveTime)
        val tvReceivedMessageParent: RelativeLayout =
            itemView.findViewById(R.id.tvReceivedMessageParent)
        val profilePictureGroupInReceiveBox: CardView =
            itemView.findViewById(R.id.profilePictureGroupInReceiveBox)
        val iv_myProfilePictureInReceiveBox: ImageView =
            itemView.findViewById(R.id.iv_myProfilePictureInReceiveBox)
    }

    private fun modifyDate(messageDate: String): String {
        val lastIndexDate = messageDate.lastIndexOf("-")
        return lastIndexDate.let { messageDate.substring(0, it) }
    }

    private fun modifyTime(messageTime: String): String {
        val lastIndexTime = messageTime.lastIndexOf(":")
        return lastIndexTime.let { messageTime.substring(0, it) }
    }

    private fun setReceiveBoxColor(senderName: String, relativeLayout: RelativeLayout) {
        var isNameAlreadyAdd = false
        var positionOfName = 0
        for (item in colorSettingList) {
            if (item == senderName) {
                isNameAlreadyAdd = true
                break
            }
            positionOfName++
        }
        if (isNameAlreadyAdd) {
            determineBgColor(positionOfName % 6, relativeLayout)
        } else {
            colorSettingList.add(senderName)
            determineBgColor(positionOfName % 6, relativeLayout)
        }
    }

    private fun determineBgColor(index: Int, relativeLayout: RelativeLayout) {
        when (index) {
            0 -> relativeLayout.background =
                ContextCompat.getDrawable(mContext, R.drawable.received_message_background1)
            1 -> relativeLayout.background =
                ContextCompat.getDrawable(mContext, R.drawable.received_message_background2)
            2 -> relativeLayout.background =
                ContextCompat.getDrawable(mContext, R.drawable.received_message_background3)
            3 -> relativeLayout.background =
                ContextCompat.getDrawable(mContext, R.drawable.received_message_background4)
            4 -> relativeLayout.background =
                ContextCompat.getDrawable(mContext, R.drawable.received_message_background5)
            5 -> relativeLayout.background =
                ContextCompat.getDrawable(mContext, R.drawable.received_message_background6)
        }
    }
}