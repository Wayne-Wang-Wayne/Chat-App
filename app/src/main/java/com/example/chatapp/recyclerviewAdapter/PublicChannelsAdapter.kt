package com.example.chatapp.recyclerviewAdapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.R
import com.example.chatapp.model.PublicChannels
import com.example.chatapp.model.User
import com.example.chatapp.model.UserChannels

class PublicChannelsAdapter(
    val context: Context,
    private var publicChannelsList: ArrayList<PublicChannels>
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view: View = LayoutInflater.from(context).inflate(R.layout.user_layout, parent, false)
        return PublicChannelsViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as UserListAdapter.UserViewHolder)

    }

    override fun getItemCount(): Int {
        return publicChannelsList.size
    }

    fun setRecyclerviewValue(
        publicChannelsList: ArrayList<PublicChannels>
    ) {
        this.publicChannelsList = publicChannelsList
        notifyDataSetChanged()
    }

    class PublicChannelsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvChannelName: TextView = itemView.findViewById(R.id.tvChannelName)
        val tvMemberAmount: TextView = itemView.findViewById(R.id.tvMemberAmount)
        val btn_joinChannel: Button = itemView.findViewById(R.id.btn_joinChannel)
    }

}