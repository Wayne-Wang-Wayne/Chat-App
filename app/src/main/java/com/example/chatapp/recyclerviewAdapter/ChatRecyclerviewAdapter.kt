package com.example.chatapp.recyclerviewAdapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.R
import com.example.chatapp.allPage.pictureDetailActivity.PictureDetailActivity
import com.example.chatapp.allPage.pictureDetailActivity.PictureDetailActivity.Companion.getDetailPictureKey
import com.example.chatapp.allPage.splash.SplashActivity.Companion.allUserProfileUrl
import com.example.chatapp.customStuff.SafeClickListener.Companion.setSafeOnClickListener
import com.example.chatapp.model.ChannelMessage
import com.example.chatapp.util.FirebaseUtil.Companion.mFirebaseAuthInstance
import com.example.chatapp.util.IntentUtil.intentToAnyClass
import com.example.chatapp.util.SmallUtil
import com.example.chatapp.util.SmallUtil.glideNormalUtil

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

            //set send time
            holder.tvSentTime.text =
                "${modifyDate(currentMessage.messageDate!!)} ${modifyTime(currentMessage.messageTime!!)}"

            //set sent message
            if (currentMessage.message == "" && currentMessage.imageUri != "") {
                //如果是圖片
                holder.ivSentImage.visibility = View.VISIBLE
                holder.tvSentMessage.visibility = View.GONE
                glideNormalUtil(mContext, currentMessage.imageUri?.toUri()!!, holder.ivSentImage)
                holder.ivSentImage.setSafeOnClickListener {
                    val bundle = Bundle()
                    bundle.putString(getDetailPictureKey,currentMessage.imageUri)
                    intentToAnyClass(mContext,bundle,PictureDetailActivity::class.java)
                }
            } else {
                //如果是文字訊息
                holder.tvSentMessage.visibility = View.VISIBLE
                holder.ivSentImage.visibility = View.GONE
                holder.tvSentMessage.text = currentMessage.message
            }
        }
        if (holder.javaClass == ReceivedViewHolder::class.java) {

            //do the stuff for received view holder
            holder as ReceivedViewHolder
            setReceiveBoxColor(currentMessage.senderName!!, holder.tvReceivedMessageParent)

            //set profile picture
            if (allUserProfileUrl[currentMessage.sentUserUID] != null) {
                SmallUtil.glideProfileUtil(
                    mContext,
                    200,
                    allUserProfileUrl[currentMessage.sentUserUID]!!,
                    holder.iv_myProfilePictureInReceiveBox
                )
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

            //set received message
            if (currentMessage.message == "" && currentMessage.imageUri != "") {
                //如果是圖片
                holder.ivReceivedImage.visibility = View.VISIBLE
                holder.tvReceivedMessage.visibility = View.GONE
                glideNormalUtil(
                    mContext,
                    currentMessage.imageUri?.toUri()!!,
                    holder.ivReceivedImage
                )
                holder.ivReceivedImage.setSafeOnClickListener {
                    val bundle = Bundle()
                    bundle.putString(getDetailPictureKey,currentMessage.imageUri)
                    intentToAnyClass(mContext,bundle,PictureDetailActivity::class.java)
                }
            } else {
                //如果是文字訊息
                holder.tvReceivedMessage.visibility = View.VISIBLE
                holder.ivReceivedImage.visibility = View.GONE
                holder.tvReceivedMessage.text = currentMessage.message
            }
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
        val ivSentImage: ImageView = itemView.findViewById(R.id.ivSentImage)
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
        val ivReceivedImage: ImageView = itemView.findViewById(R.id.ivReceivedImage)
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