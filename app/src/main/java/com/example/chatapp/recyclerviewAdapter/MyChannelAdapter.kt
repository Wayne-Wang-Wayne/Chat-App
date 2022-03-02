package com.example.chatapp.recyclerviewAdapter

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.R
import com.example.chatapp.allPage.chatActivity.ChatActivity
import com.example.chatapp.customStuff.SafeClickListener.Companion.setSafeOnClickListener
import com.example.chatapp.model.PublicChannels
import com.example.chatapp.model.UserChannels
import com.example.chatapp.util.IntentUtil.intentToAnyClass

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
        holder.tv_ChannelName.text = myChannelsList[position].channelsName
        if (myChannelsList[position].lastSenderName != "") {
            holder.tv_LastMessage.text =
                "${myChannelsList[position].lastSenderName}:${myChannelsList[position].lastMessage}"
        } else {
            holder.tv_LastMessage.text = ""
        }
        if (myChannelsList[position].needNewTag == true) {
            holder.iv_NewTag.visibility = View.VISIBLE
        } else {
            holder.iv_NewTag.visibility = View.INVISIBLE
        }
        var recentTime = myChannelsList[position].updateTime
        var lastIndex = recentTime?.lastIndexOf(":");
        recentTime = lastIndex?.let { recentTime?.substring(0, it) };
        holder.tv_CurrentTime.text =
            "${myChannelsList[position].updateDate}\n$recentTime"
        holder.itemView.setSafeOnClickListener {
            val mBundle = Bundle()
            mBundle.apply {
                putString("channelName", myChannelsList[position].channelsName)
                putString("channelUID", myChannelsList[position].channelUID)
            }
            intentToAnyClass(context,mBundle,ChatActivity::class.java)
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
        val iv_NewTag = itemView.findViewById<ImageView>(R.id.iv_NewTag)
        val tv_CurrentTime = itemView.findViewById<TextView>(R.id.tv_CurrentTime)
    }
}