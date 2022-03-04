package com.example.chatapp.recyclerviewAdapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.R
import com.example.chatapp.model.ChannelMessage
import com.example.chatapp.util.FirebaseUtil.Companion.mFirebaseAuthInstance

class ChatRecyclerviewAdapter(
    private val mContext: Context,
    private val messageList: ArrayList<ChannelMessage>
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

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
            holder.tvSentMessage.text = currentMessage.message
            holder.tvSentTime.text = "${modifyDate(currentMessage.messageDate!!)} ${modifyTime(currentMessage.messageTime!!)}"
        }
        if (holder.javaClass == ReceivedViewHolder::class.java) {
            //do the stuff for received view holder
            holder as ReceivedViewHolder
            holder.tvReceivedMessage.text = currentMessage.message
            holder.tvReceiveName.text = currentMessage.senderName
            holder.tvReceiveTime.text = "${modifyDate(currentMessage.messageDate!!)} ${modifyTime(currentMessage.messageTime!!)}"
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
    }

    private fun modifyDate(messageDate: String): String {
        val lastIndexDate = messageDate.lastIndexOf("-")
        return lastIndexDate.let { messageDate.substring(0, it) }
    }

    private fun modifyTime(messageTime: String): String {
        val lastIndexTime = messageTime.lastIndexOf(":")
        return lastIndexTime.let { messageTime.substring(0, it) }
    }
}