package com.example.chatapp.recyclerviewAdapter

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.example.chatapp.R
import com.example.chatapp.allPage.chatActivity.ChatActivity
import com.example.chatapp.allPage.myInfoActivity.MyInfoActivity.Companion.MainPicture
import com.example.chatapp.allPage.splash.SplashActivity
import com.example.chatapp.allPage.splash.SplashActivity.Companion.allUserProfileUrl
import com.example.chatapp.customStuff.SafeClickListener.Companion.setSafeOnClickListener
import com.example.chatapp.model.PublicChannels
import com.example.chatapp.model.UserChannels
import com.example.chatapp.util.IntentUtil.intentToAnyClass
import com.example.chatapp.util.SmallUtil
import com.example.chatapp.util.SmallUtil.getCharCount

class MyChannelAdapter(
    val context: Context,
    private var myChannelsList: ArrayList<UserChannels>
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view: View =
            LayoutInflater.from(context).inflate(R.layout.item_my_channel, parent, false)
        return MyChannelsViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as MyChannelsViewHolder)

        //set channel Name
        holder.tv_ChannelName.text = myChannelsList[position].channelsName

        //set channel last message and last sender's profile picture
        if (myChannelsList[position].lastSenderName != "") {

            if (myChannelsList[position].lastImageUriSent != "") {
                //如果是send圖片
                holder.tv_LastMessage.text = "${myChannelsList[position].lastSenderName}傳送了圖片。"
            } else if (myChannelsList[position].lastMessage != "") {
                //如果是send message
                val charAmount = getCharCount(myChannelsList[position].lastMessage!!)
                if (charAmount >= 11) {
                    holder.tv_LastMessage.text =
                        "${myChannelsList[position].lastSenderName}:${
                            myChannelsList[position].lastMessage?.substring(
                                0,
                                10
                            )
                        }..."
                } else {
                    holder.tv_LastMessage.text =
                        "${myChannelsList[position].lastSenderName}:${myChannelsList[position].lastMessage}"
                }
            } else if (myChannelsList[position].lastVideoUriSent != "") {
                //如果是send 影片
                holder.tv_LastMessage.text = "${myChannelsList[position].lastSenderName}傳送了影片。"
            }else if (myChannelsList[position].lastVoiceUriSent != "") {
                //如果是send 語音訊息
                holder.tv_LastMessage.text = "${myChannelsList[position].lastSenderName}傳送了語音訊息。"
            }
            //set last sender's profile picture
            holder.profilePictureGroupInMyChannel.visibility = View.VISIBLE
            if (allUserProfileUrl[myChannelsList[position].lastSenderUid] != null) {
                SmallUtil.glideProfileUtil(
                    context,
                    300,
                    MainPicture,
                    SplashActivity.allUserProfileUrl[myChannelsList[position].lastSenderUid]!!,
                    holder.iv_myProfilePictureInMyChannel
                )
            } else {
                holder.iv_myProfilePictureInMyChannel.setImageResource(R.drawable.default_user_image)
            }
        } else {
            holder.tv_LastMessage.text = ""
            holder.profilePictureGroupInMyChannel.visibility = View.INVISIBLE
        }
        //判斷是否須加new tag
        if (myChannelsList[position].needNewTag == true) {
            holder.iv_NewTag.visibility = View.VISIBLE
        } else {
            holder.iv_NewTag.visibility = View.INVISIBLE
        }
        //set 時間
        var recentTime = myChannelsList[position].updateTime
        var lastIndex = recentTime?.lastIndexOf(":");
        recentTime = lastIndex?.let { recentTime?.substring(0, it) }
        holder.tv_CurrentTime.text =
            "${myChannelsList[position].updateDate}\n$recentTime"
        //上色
        if (position % 2 == 1) {
            holder.itemView.setBackgroundColor(
                ContextCompat.getColor(context, R.color.my_channel_bg_one)
            )
        } else {
            holder.itemView.setBackgroundColor(
                ContextCompat.getColor(context, R.color.my_channel_bg_two)
            )
        }

        //set item click logic
        holder.itemView.setSafeOnClickListener {
            val mBundle = Bundle()
            mBundle.apply {
                putString("channelName", myChannelsList[position].channelsName)
                putString("channelUID", myChannelsList[position].channelUID)
            }
            intentToAnyClass(context, mBundle, ChatActivity::class.java)
        }
    }

    override fun getItemCount(): Int {
        return myChannelsList.size
    }

    fun setRecyclerviewValue(
        myChannelsList: ArrayList<UserChannels>
    ) {
        this.myChannelsList = myChannelsList
        notifyDataSetChanged()
    }

    class MyChannelsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tv_ChannelName = itemView.findViewById<TextView>(R.id.tv_ChannelName)
        val tv_LastMessage = itemView.findViewById<TextView>(R.id.tv_LastMessage)
        val iv_NewTag = itemView.findViewById<LottieAnimationView>(R.id.iv_NewTag)
        val tv_CurrentTime = itemView.findViewById<TextView>(R.id.tv_CurrentTime)
        val profilePictureGroupInMyChannel =
            itemView.findViewById<CardView>(R.id.profilePictureGroupInMyChannel)
        val iv_myProfilePictureInMyChannel =
            itemView.findViewById<ImageView>(R.id.iv_myProfilePictureInMyChannel)
    }
}