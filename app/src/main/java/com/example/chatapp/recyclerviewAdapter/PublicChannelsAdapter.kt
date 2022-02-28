package com.example.chatapp.recyclerviewAdapter

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.R
import com.example.chatapp.customStuff.SafeClickListener.Companion.setSafeOnClickListener
import com.example.chatapp.model.PublicChannels
import com.example.chatapp.model.User
import com.example.chatapp.model.UserChannels
import com.example.chatapp.util.FirebaseUtil.Companion.joinChannel

class PublicChannelsAdapter(
    val context: Context,
    private var publicChannelsList: ArrayList<PublicChannels>
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view: View =
            LayoutInflater.from(context).inflate(R.layout.item_public_channel, parent, false)
        return PublicChannelsViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as PublicChannelsViewHolder)
        holder.tvChannelName.text = publicChannelsList[position].channelName
        holder.tvMemberAmount.text = "${publicChannelsList[position].userAmount.toString()}位成員"
        holder.btn_joinChannel.setSafeOnClickListener {
            AlertDialog.Builder(context)
                .setTitle("加入群組")
                .setMessage("確定要加入此群組？")
                .setPositiveButton("確定") { _, _ ->
                    publicChannelsList[position].channelUID?.let { it1 ->
                        joinChannel(
                            context,
                            it1
                        )
                    }
                }
                .setNegativeButton("離開") { _, _ -> }
                .show()
        }
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